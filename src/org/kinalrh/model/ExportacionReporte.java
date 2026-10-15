package org.kinalrh.model;

import java.time.LocalDateTime;

public record ExportacionReporte(Long idExportacion, String folio, long idUsuario,
        String tipoReporte, LocalDateTime fechaHora) { }
