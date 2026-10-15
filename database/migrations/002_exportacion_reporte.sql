CREATE TABLE exportacion_reporte (
    id_exportacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    folio CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    id_usuario BIGINT UNSIGNED NOT NULL,
    tipo_reporte VARCHAR(80) NOT NULL,
    fecha_hora DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_exportacion_folio UNIQUE (folio),
    CONSTRAINT fk_exportacion_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_exportacion_usuario_fecha (id_usuario, fecha_hora)
) ENGINE=InnoDB;
