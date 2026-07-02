package es.franciscorodalf.safeinvestor.bancos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.bancos.client.GoCardlessClient;
import es.franciscorodalf.safeinvestor.bancos.domain.BankAccount;
import es.franciscorodalf.safeinvestor.bancos.domain.BankConnection;
import es.franciscorodalf.safeinvestor.bancos.domain.BankConnectionRepository;
import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;
import es.franciscorodalf.safeinvestor.movimientos.domain.CategoriaRepository;
import es.franciscorodalf.safeinvestor.movimientos.domain.Movimiento;
import es.franciscorodalf.safeinvestor.movimientos.domain.MovimientoRepository;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import es.franciscorodalf.safeinvestor.movimientos.service.CsvImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Orquestación de la integración bancaria:
 *   startLink()  crea el requisition y devuelve la URL a la que redirigir
 *                al usuario para que autorice en su banco.
 *   finishLink() se llama al volver del banco: recupera accounts, guarda
 *                BankAccount, marca la conexión como LINKED y hace un
 *                primer sync inmediato.
 *   sync()       trae las transacciones nuevas y las inserta como
 *                movimientos, dedup por transactionId contra external_id.
 */
@Service
public class BancoService {

    private static final Logger log = LoggerFactory.getLogger(BancoService.class);

    private final GoCardlessClient gocardless;
    private final BankConnectionRepository connections;
    private final MovimientoRepository movimientos;
    private final CategoriaRepository categorias;
    private final CsvImportService csvImportService;

    public BancoService(GoCardlessClient gocardless,
                        BankConnectionRepository connections,
                        MovimientoRepository movimientos,
                        CategoriaRepository categorias,
                        CsvImportService csvImportService) {
        this.gocardless = gocardless;
        this.connections = connections;
        this.movimientos = movimientos;
        this.categorias = categorias;
        this.csvImportService = csvImportService;
    }

    // ============================================================
    // Descubrimiento de bancos
    // ============================================================

    public List<Map<String, Object>> listInstitutions(String country) {
        if (!gocardless.isConfigured()) return List.of();
        String c = country == null || country.isBlank()
                ? gocardless.getProperties().defaultCountry()
                : country;
        return gocardless.listInstitutions(c);
    }

    // ============================================================
    // Ciclo de vida de una conexión
    // ============================================================

    @Transactional
    public String startLink(Usuario usuario, String institutionId, String institutionName, String institutionLogo) {
        if (!gocardless.isConfigured()) {
            throw new IllegalStateException("GoCardless no está configurado");
        }
        // Reference único para identificar el requisition en el callback si algo falla
        String reference = "safeinvestor-" + usuario.getId() + "-" + UUID.randomUUID();
        Map<String, Object> res = gocardless.createRequisition(institutionId, reference);
        String requisitionId = (String) res.get("id");
        String link          = (String) res.get("link");

        BankConnection conn = new BankConnection(usuario, requisitionId,
                institutionId, institutionName, institutionLogo);
        connections.save(conn);
        return link;
    }

    /**
     * Callback tras la autorización en el banco. Trae accounts, las guarda y hace sync inmediato.
     * @return la conexión actualizada
     */
    @Transactional
    public BankConnection finishLink(Usuario usuario, String requisitionId) {
        BankConnection conn = connections.findByRequisitionId(requisitionId)
                .orElseThrow(() -> new IllegalArgumentException("Conexión no encontrada"));
        if (!conn.getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("La conexión no pertenece a este usuario");
        }
        Map<String, Object> req = gocardless.getRequisition(requisitionId);
        String status = (String) req.getOrDefault("status", "UNKNOWN");
        @SuppressWarnings("unchecked")
        List<String> accountIds = (List<String>) req.getOrDefault("accounts", List.of());

        if (!"LN".equals(status) && accountIds.isEmpty()) {
            // Aún no autorizada — puede ser CR (created), GC (giving consent), UA (undergoing authentication)
            log.info("Requisition {} en estado {} sin cuentas todavía", requisitionId, status);
            return conn;
        }

        for (String accId : accountIds) {
            // Si ya existía la cuenta, no duplicamos
            boolean already = conn.getAccounts().stream().anyMatch(a -> a.getExternalId().equals(accId));
            if (already) continue;
            try {
                Map<String, Object> details = gocardless.getAccountDetails(accId);
                @SuppressWarnings("unchecked")
                Map<String, Object> acc = (Map<String, Object>) details.get("account");
                String iban     = acc != null ? (String) acc.get("iban") : null;
                String name     = acc != null ? (String) acc.getOrDefault("name",
                                                    acc.getOrDefault("ownerName", "Cuenta")) : "Cuenta";
                String currency = acc != null ? (String) acc.get("currency") : "EUR";
                conn.addAccount(new BankAccount(accId, iban, String.valueOf(name), currency));
            } catch (Exception e) {
                log.warn("No se pudieron obtener detalles de la cuenta {}: {}", accId, e.getMessage());
                conn.addAccount(new BankAccount(accId, null, "Cuenta", "EUR"));
            }
        }

        conn.markLinked(OffsetDateTime.now().plusDays(90));
        connections.save(conn);

        // Sync inicial
        try {
            sync(usuario, conn.getId());
        } catch (Exception e) {
            log.warn("Sync inicial falló para conexión {}: {}", conn.getId(), e.getMessage());
        }
        return conn;
    }

    // ============================================================
    // Sync de transacciones
    // ============================================================

    @Transactional
    public SyncResult sync(Usuario usuario, Long connectionId) {
        BankConnection conn = connections.findByIdAndUsuario(connectionId, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Conexión no encontrada"));
        if (conn.getStatus() != BankConnection.Status.LINKED) {
            throw new IllegalStateException("La conexión no está activa: " + conn.getStatus());
        }

        List<Categoria> userCats = categorias.findByUsuarioOrderByNombre(usuario);
        int created = 0, skipped = 0, errored = 0;

        for (BankAccount account : conn.getAccounts()) {
            try {
                Map<String, Object> resp = gocardless.getAccountTransactions(account.getExternalId());
                @SuppressWarnings("unchecked")
                Map<String, Object> transactions = (Map<String, Object>) resp.get("transactions");
                if (transactions == null) continue;
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> booked = (List<Map<String, Object>>) transactions.getOrDefault("booked", List.of());
                for (Map<String, Object> tx : booked) {
                    var outcome = importTransaction(usuario, tx, userCats);
                    if (outcome == ImportOutcome.CREATED) created++;
                    else if (outcome == ImportOutcome.SKIPPED) skipped++;
                    else errored++;
                }
            } catch (Exception e) {
                log.warn("Error leyendo transacciones de la cuenta {}: {}", account.getExternalId(), e.getMessage());
                errored++;
            }
        }

        conn.markSynced();
        connections.save(conn);
        log.info("Sync conexión {}: {} creados, {} duplicados, {} errores", conn.getId(), created, skipped, errored);
        return new SyncResult(created, skipped, errored);
    }

    private enum ImportOutcome { CREATED, SKIPPED, ERROR }

    private ImportOutcome importTransaction(Usuario usuario, Map<String, Object> tx, List<Categoria> userCats) {
        try {
            String externalId = firstString(tx.get("transactionId"), tx.get("internalTransactionId"));
            if (externalId == null || externalId.isBlank()) {
                // Sin ID único no podemos dedup — mejor saltar que crear duplicados
                return ImportOutcome.SKIPPED;
            }
            if (movimientos.existsByUsuarioAndExternalId(usuario, externalId)) {
                return ImportOutcome.SKIPPED;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> amountObj = (Map<String, Object>) tx.get("transactionAmount");
            if (amountObj == null) return ImportOutcome.ERROR;
            BigDecimal amount = new BigDecimal(String.valueOf(amountObj.get("amount")));
            TipoMovimiento tipo = amount.signum() < 0 ? TipoMovimiento.GASTO : TipoMovimiento.INGRESO;
            BigDecimal importe = amount.abs();

            String fechaStr = firstString(tx.get("bookingDate"), tx.get("valueDate"));
            LocalDate fecha = fechaStr != null ? LocalDate.parse(fechaStr) : LocalDate.now();

            String descripcion = firstString(
                    tx.get("remittanceInformationUnstructured"),
                    tx.get("creditorName"),
                    tx.get("debtorName"),
                    tx.get("additionalInformation")
            );
            if (descripcion != null && descripcion.length() > 200) {
                descripcion = descripcion.substring(0, 200);
            }

            Long categoriaId = csvImportService.suggestCategoryId(descripcion, userCats);

            Movimiento m = new Movimiento(usuario,
                    categoriaId != null
                            ? userCats.stream().filter(c -> c.getId().equals(categoriaId)).findFirst().orElse(null)
                            : null,
                    tipo, importe, descripcion, fecha);
            m.setExternalId(externalId);
            movimientos.save(m);
            return ImportOutcome.CREATED;
        } catch (Exception e) {
            log.warn("Error importando transacción: {}", e.getMessage());
            return ImportOutcome.ERROR;
        }
    }

    private static String firstString(Object... values) {
        for (Object v : values) {
            if (v != null) {
                String s = String.valueOf(v).trim();
                if (!s.isEmpty()) return s;
            }
        }
        return null;
    }

    // ============================================================
    // Lecturas + borrado
    // ============================================================

    public List<BankConnection> findAll(Usuario usuario) {
        return connections.findByUsuarioOrderByCreatedAtDesc(usuario);
    }

    public List<BankConnection> findAllLinked() {
        return connections.findByStatus(BankConnection.Status.LINKED);
    }

    @Transactional
    public void revoke(Usuario usuario, Long connectionId) {
        BankConnection conn = connections.findByIdAndUsuario(connectionId, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Conexión no encontrada"));
        gocardless.deleteRequisition(conn.getRequisitionId());
        connections.delete(conn);
    }

    public record SyncResult(int created, int skipped, int errored) {}
}
