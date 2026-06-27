package es.franciscorodalf.safeinvestor.movimientos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del parser de CSV bancario.
 *
 * Cubrimos:
 *   - Auto-detección de separador (',', ';', tab).
 *   - Auto-detección de decimal (coma vs punto).
 *   - Varios formatos de fecha en una misma corrida.
 *   - Signo del importe → tipo GASTO/INGRESO.
 *   - Filtrado de \"años sueltos\" (1900-2100) como falso importe.
 *   - Comillas dobles alrededor de campos con separador interno.
 *   - Auto-sugerencia de categoría por keyword.
 *   - Fallback a ISO-8859-1 cuando el contenido no es UTF-8 válido.
 *   - Líneas vacías y \"no parseables\" se contabilizan como skipped.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CsvImportServiceTest {

    @Autowired UsuarioService usuarioService;
    @Autowired CsvImportService importService;

    private Usuario usuario(String email) {
        return usuarioService.register(email, "Test", "password123");
    }

    @Test
    void parseaCsvEspañolEstandar_separadorPuntoYComa_decimalConComa() {
        Usuario u = usuario("csv-es@x.com");
        String csv = """
                Fecha;Importe;Concepto
                15/06/2026;-42,50;MERCADONA Compra semanal
                14/06/2026;1.250,00;Nomina Junio
                """;
        var result = importService.parse(u, csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(';', result.separator);
        assertEquals(2, result.rows.size(), "Debe descartar la cabecera y parsear 2 filas");

        var gasto = result.rows.get(0);
        assertEquals(LocalDate.of(2026, 6, 15), gasto.fecha);
        assertEquals(TipoMovimiento.GASTO, gasto.tipo);
        assertEquals(0, gasto.importe.compareTo(new BigDecimal("42.50")));

        var ingreso = result.rows.get(1);
        assertEquals(TipoMovimiento.INGRESO, ingreso.tipo);
        assertEquals(0, ingreso.importe.compareTo(new BigDecimal("1250.00")));
    }

    @Test
    void parseaCsvAmericano_separadorComa_decimalConPunto() {
        Usuario u = usuario("csv-us@x.com");
        String csv = """
                Date,Amount,Description
                2026-06-15,-42.50,Coffee shop
                2026-06-14,1250.00,Paycheck
                """;
        var result = importService.parse(u, csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(',', result.separator);
        assertEquals(2, result.rows.size());
        assertEquals(LocalDate.of(2026, 6, 15), result.rows.get(0).fecha);
        assertEquals(TipoMovimiento.GASTO, result.rows.get(0).tipo);
    }

    @Test
    void respetaComillasAlrededorDeCampoConSeparadorInterno() {
        Usuario u = usuario("csv-quotes@x.com");
        String csv = """
                Fecha;Importe;Concepto
                10/06/2026;-25,00;"Cena con amigos; sin postre"
                """;
        var result = importService.parse(u, csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, result.rows.size());
        // El \";\" interno NO debe partir el campo descripción
        assertTrue(result.rows.get(0).descripcion.contains("Cena con amigos"));
        assertTrue(result.rows.get(0).descripcion.contains("sin postre"));
    }

    @Test
    void filtraAñosSueltosComoFalsosImportes() {
        Usuario u = usuario("csv-year@x.com");
        // Una columna \"2026\" sola (sin decimales) NO debe parsearse como importe;
        // el parser tiene que escoger -15,00 como importe real.
        String csv = "15/06/2026;2026;-15,00;Compra\n";
        var result = importService.parse(u, csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, result.rows.size());
        assertEquals(0, result.rows.get(0).importe.compareTo(new BigDecimal("15.00")),
                "El importe debe ser 15.00 — 2026 (año) no debe ganarle como importe");
    }

    @Test
    void lineasVaciasYNoParseables_sumanASkipped() {
        Usuario u = usuario("csv-skip@x.com");
        String csv = """
                Fecha;Importe;Concepto
                15/06/2026;-10,00;Algo

                solo texto sin nada útil
                14/06/2026;+5,00;Otro
                """;
        var result = importService.parse(u, csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(2, result.rows.size(), "Solo dos filas deberían parsearse");
        assertTrue(result.skippedLines >= 2, "La cabecera + linea vacía + linea basura deberían contar como skipped");
    }

    @Test
    void sugiereCategoriaSiDescripcionMatcheaKeyword() {
        Usuario u = usuario("csv-cat@x.com");
        // NETFLIX es keyword del canónico \"Ocio\", que coincide con la default \"Ocio\"
        String csv = "15/06/2026;-9,99;NETFLIX suscripcion\n";
        var result = importService.parse(u, csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, result.rows.size());
        assertNotNull(result.rows.get(0).categoriaIdSugerida,
                "NETFLIX debería sugerir la categoría Ocio");
    }

    @Test
    void noSugiereCategoriaSiNoHayMatching() {
        Usuario u = usuario("csv-nocat@x.com");
        String csv = "15/06/2026;-10,00;Compra random xyz\n";
        var result = importService.parse(u, csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, result.rows.size());
        assertNull(result.rows.get(0).categoriaIdSugerida);
    }

    @Test
    void aceptaVariosFormatosDeFechaEnMismaCorrida() {
        Usuario u = usuario("csv-dates@x.com");
        String csv = """
                15/06/2026;-1,00;A
                15-06-2026;-2,00;B
                2026-06-15;-3,00;C
                15.06.2026;-4,00;D
                """;
        var result = importService.parse(u, csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(4, result.rows.size());
        result.rows.forEach(r -> assertEquals(LocalDate.of(2026, 6, 15), r.fecha));
    }

    @Test
    void detectaEncodingIso8859_1ConCaracteresEspañoles() {
        Usuario u = usuario("csv-iso@x.com");
        // Simulamos un export bancario en ISO-8859-1 (Latin-1) con \"ñ\" y \"é\"
        String text = "15/06/2026;-50,00;Compra en cafetería del señor\n";
        byte[] iso = text.getBytes(java.nio.charset.Charset.forName("ISO-8859-1"));
        var result = importService.parse(u, iso);

        assertEquals(1, result.rows.size());
        assertTrue(result.rows.get(0).descripcion.contains("cafetería"));
        assertTrue(result.rows.get(0).descripcion.contains("señor"));
    }

    @Test
    void truncaDescripcionA200Chars() {
        Usuario u = usuario("csv-long@x.com");
        String desc = "x".repeat(300);
        String csv = "15/06/2026;-1,00;" + desc + "\n";
        var result = importService.parse(u, csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, result.rows.size());
        assertEquals(200, result.rows.get(0).descripcion.length());
    }

    @Test
    void csvVacioDevuelveListaVacia() {
        Usuario u = usuario("csv-empty@x.com");
        var result = importService.parse(u, "".getBytes(StandardCharsets.UTF_8));

        assertEquals(0, result.rows.size());
    }

    @Test
    void retiraBOMUtf8AlInicio() {
        Usuario u = usuario("csv-bom@x.com");
        String csv = "﻿15/06/2026;-10,00;Algo\n";
        var result = importService.parse(u, csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, result.rows.size(), "El BOM no debe impedir parsear la primera línea");
        assertEquals(LocalDate.of(2026, 6, 15), result.rows.get(0).fecha);
    }
}
