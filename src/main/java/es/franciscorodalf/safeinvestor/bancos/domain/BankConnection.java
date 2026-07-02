package es.franciscorodalf.safeinvestor.bancos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Conexión con un banco vía GoCardless. Representa el consentimiento
 * del usuario para que la app lea sus movimientos durante ~90 días.
 */
@Entity
@Table(name = "bank_connections")
public class BankConnection {

    public enum Status { PENDING, LINKED, EXPIRED, REVOKED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "requisition_id", nullable = false, length = 64)
    private String requisitionId;

    @Column(name = "institution_id", nullable = false, length = 64)
    private String institutionId;

    @Column(name = "institution_name", nullable = false, length = 200)
    private String institutionName;

    @Column(name = "institution_logo", length = 500)
    private String institutionLogo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "linked_at")
    private OffsetDateTime linkedAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "last_sync_at")
    private OffsetDateTime lastSyncAt;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "connection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BankAccount> accounts = new ArrayList<>();

    protected BankConnection() {}

    public BankConnection(Usuario usuario, String requisitionId, String institutionId,
                          String institutionName, String institutionLogo) {
        this.usuario = usuario;
        this.requisitionId = requisitionId;
        this.institutionId = institutionId;
        this.institutionName = institutionName;
        this.institutionLogo = institutionLogo;
    }

    public void markLinked(OffsetDateTime expiresAt) {
        this.status = Status.LINKED;
        this.linkedAt = OffsetDateTime.now();
        this.expiresAt = expiresAt;
    }

    public void markSynced() { this.lastSyncAt = OffsetDateTime.now(); }
    public void markRevoked() { this.status = Status.REVOKED; }
    public void markExpired() { this.status = Status.EXPIRED; }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getRequisitionId() { return requisitionId; }
    public String getInstitutionId() { return institutionId; }
    public String getInstitutionName() { return institutionName; }
    public String getInstitutionLogo() { return institutionLogo; }
    public Status getStatus() { return status; }
    public OffsetDateTime getLinkedAt() { return linkedAt; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public OffsetDateTime getLastSyncAt() { return lastSyncAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<BankAccount> getAccounts() { return accounts; }
    public void addAccount(BankAccount a) { accounts.add(a); a.setConnection(this); }
}
