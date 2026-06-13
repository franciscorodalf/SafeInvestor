package es.franciscorodalf.safeinvestor.movimientos.web;

import es.franciscorodalf.safeinvestor.movimientos.domain.Movimiento;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.MovimientoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/movimientos")
public class MovimientoExportController {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final MovimientoService movimientos;
    private final CurrentUser currentUser;

    public MovimientoExportController(MovimientoService movimientos, CurrentUser currentUser) {
        this.movimientos = movimientos;
        this.currentUser = currentUser;
    }

    /** Exporta movimientos a CSV (utf-8 con BOM para Excel). */
    @GetMapping(value = "/export.csv", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<byte[]> exportCsv(
        @RequestParam(required = false) Long categoriaId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        var u = currentUser.get();
        // Cogemos hasta 1000 movimientos en el rango
        var page = movimientos.search(u, categoriaId, desde, hasta, 0);
        List<Movimiento> rows = page.getContent();

        StringBuilder sb = new StringBuilder();
        // BOM para Excel
        sb.append('﻿');
        sb.append("Fecha;Tipo;Importe;Categoria;Descripcion\n");
        for (Movimiento m : rows) {
            sb.append(m.getFecha().format(ISO)).append(';');
            sb.append(m.getTipo().name()).append(';');
            sb.append(m.getImporte().toPlainString().replace('.', ',')).append(';');
            sb.append(escape(m.getCategoria() != null ? m.getCategoria().getNombre() : "")).append(';');
            sb.append(escape(m.getDescripcion() != null ? m.getDescripcion() : "")).append('\n');
        }
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "movimientos-" + LocalDate.now() + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);
        return new ResponseEntity<>(body, headers, 200);
    }

    private static String escape(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(";") || s.contains("\"") || s.contains("\n");
        String escaped = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }
}
