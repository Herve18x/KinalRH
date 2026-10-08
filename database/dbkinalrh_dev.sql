-- ============================================================
-- IN4CM
-- Sistema de Gestión de Recursos Humanos - Fundación Kinal
-- MySQL 8.x
-- Diseño físico inicial
-- Fecha: 2026-08-17
-- ============================================================

DROP DATABASE IF EXISTS IN4CM;
CREATE DATABASE IN4CM
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE IN4CM;

-- ============================================================
-- 1. SEGURIDAD
-- ============================================================

CREATE TABLE rol (
    id_rol BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE permiso (
    id_permiso BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    modulo VARCHAR(80) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_permiso_codigo UNIQUE (codigo)
) ENGINE=InnoDB;

CREATE TABLE area (
    id_area BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_area_codigo UNIQUE (codigo),
    CONSTRAINT uq_area_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE puesto (
    id_puesto BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_puesto_codigo UNIQUE (codigo),
    CONSTRAINT uq_puesto_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE estado_empleado (
    id_estado_empleado BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255) NULL,
    es_estado_activo BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_estado_empleado_codigo UNIQUE (codigo),
    CONSTRAINT uq_estado_empleado_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE nivel_academico (
    id_nivel_academico BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    orden_nivel INT NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_nivel_academico_codigo UNIQUE (codigo),
    CONSTRAINT uq_nivel_academico_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE tipo_contrato (
    id_tipo_contrato BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    requiere_fecha_fin BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_tipo_contrato_codigo UNIQUE (codigo),
    CONSTRAINT uq_tipo_contrato_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE tipo_logro (
    id_tipo_logro BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_tipo_logro_codigo UNIQUE (codigo),
    CONSTRAINT uq_tipo_logro_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE tipo_documento (
    id_tipo_documento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_tipo_documento_codigo UNIQUE (codigo),
    CONSTRAINT uq_tipo_documento_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE empleado (
    id_empleado BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    dpi VARCHAR(20) NOT NULL,
    nit VARCHAR(30) NULL,
    codigo_empleado VARCHAR(50) NULL,

    primer_nombre VARCHAR(80) NOT NULL,
    segundo_nombre VARCHAR(80) NULL,
    tercer_nombre VARCHAR(80) NULL,
    primer_apellido VARCHAR(80) NOT NULL,
    segundo_apellido VARCHAR(80) NULL,
    apellido_casada VARCHAR(80) NULL,

    nombre_completo VARCHAR(255) NULL,

    fecha_nacimiento DATE NULL,
    estado_civil VARCHAR(50) NULL,
    religion VARCHAR(100) NULL,

    direccion VARCHAR(500) NULL,
    zona VARCHAR(20) NULL,
    municipio VARCHAR(120) NULL,
    departamento VARCHAR(120) NULL,

    telefono_movil VARCHAR(30) NULL,
    telefono_fijo VARCHAR(30) NULL,
    correo_personal VARCHAR(150) NULL,

    fotografia_path VARCHAR(500) NULL,
    dpi_documento_path VARCHAR(500) NULL,

    fecha_ingreso_kinal DATE NULL,

    id_estado_empleado BIGINT UNSIGNED NOT NULL,
    id_nivel_academico BIGINT UNSIGNED NULL,
    id_puesto_actual BIGINT UNSIGNED NULL,
    id_area_principal BIGINT UNSIGNED NULL,
    id_jefe_inmediato BIGINT UNSIGNED NULL,

    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_empleado_dpi UNIQUE (dpi),
    CONSTRAINT uq_empleado_nit UNIQUE (nit),
    CONSTRAINT uq_empleado_codigo UNIQUE (codigo_empleado),

    CONSTRAINT fk_empleado_estado
        FOREIGN KEY (id_estado_empleado)
        REFERENCES estado_empleado (id_estado_empleado),

    CONSTRAINT fk_empleado_nivel_academico
        FOREIGN KEY (id_nivel_academico)
        REFERENCES nivel_academico (id_nivel_academico)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_empleado_puesto
        FOREIGN KEY (id_puesto_actual)
        REFERENCES puesto (id_puesto)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_empleado_area_principal
        FOREIGN KEY (id_area_principal)
        REFERENCES area (id_area)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_empleado_jefe
        FOREIGN KEY (id_jefe_inmediato)
        REFERENCES empleado (id_empleado)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_empleado_nombre (nombre_completo),
    INDEX idx_empleado_estado (id_estado_empleado),
    INDEX idx_empleado_area_principal (id_area_principal),
    INDEX idx_empleado_puesto (id_puesto_actual),
    INDEX idx_empleado_nivel (id_nivel_academico),
    INDEX idx_empleado_fecha_ingreso (fecha_ingreso_kinal)
) ENGINE=InnoDB;

CREATE TABLE empleado_area (
    id_empleado BIGINT UNSIGNED NOT NULL,
    id_area BIGINT UNSIGNED NOT NULL,
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_inicio DATE NULL,
    fecha_fin DATE NULL,

    PRIMARY KEY (id_empleado, id_area),

    CONSTRAINT fk_empleado_area_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES empleado (id_empleado)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_empleado_area_area
        FOREIGN KEY (id_area)
        REFERENCES area (id_area)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_empleado_area_area (id_area),
    INDEX idx_empleado_area_principal (id_empleado, es_principal)
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(80) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(160) NOT NULL,
    correo VARCHAR(150) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    id_empleado BIGINT UNSIGNED NULL,

    ultimo_acceso_en DATETIME NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario),
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT uq_usuario_empleado UNIQUE (id_empleado),

    CONSTRAINT fk_usuario_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES empleado (id_empleado)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE usuario_rol (
    id_usuario BIGINT UNSIGNED NOT NULL,
    id_rol BIGINT UNSIGNED NOT NULL,

    PRIMARY KEY (id_usuario, id_rol),

    CONSTRAINT fk_usuario_rol_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_usuario_rol_rol
        FOREIGN KEY (id_rol)
        REFERENCES rol (id_rol)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE rol_permiso (
    id_rol BIGINT UNSIGNED NOT NULL,
    id_permiso BIGINT UNSIGNED NOT NULL,

    PRIMARY KEY (id_rol, id_permiso),

    CONSTRAINT fk_rol_permiso_rol
        FOREIGN KEY (id_rol)
        REFERENCES rol (id_rol)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_rol_permiso_permiso
        FOREIGN KEY (id_permiso)
        REFERENCES permiso (id_permiso)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 2. CONTACTOS
-- ============================================================

CREATE TABLE contacto_emergencia (
    id_contacto_emergencia BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_empleado BIGINT UNSIGNED NOT NULL,
    nombre VARCHAR(160) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    parentesco VARCHAR(80) NULL,
    es_principal BOOLEAN NOT NULL DEFAULT TRUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_contacto_emergencia_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES empleado (id_empleado)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_contacto_emergencia_empleado (id_empleado)
) ENGINE=InnoDB;

-- ============================================================
-- 3. CONTRATOS
-- ============================================================

CREATE TABLE contrato (
    id_contrato BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_empleado BIGINT UNSIGNED NOT NULL,
    id_tipo_contrato BIGINT UNSIGNED NOT NULL,
    numero_contrato VARCHAR(80) NULL,
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NULL,
    jornada VARCHAR(100) NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'VIGENTE',
    observaciones VARCHAR(500) NULL,

    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_contrato_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES empleado (id_empleado)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_contrato_tipo
        FOREIGN KEY (id_tipo_contrato)
        REFERENCES tipo_contrato (id_tipo_contrato)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_contrato_empleado (id_empleado),
    INDEX idx_contrato_vigencia (fecha_inicio, fecha_fin),
    INDEX idx_contrato_estado (estado)
) ENGINE=InnoDB;

CREATE TABLE historial_salarial (
    id_historial_salarial BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_empleado BIGINT UNSIGNED NOT NULL,

    salario_base DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    bonificacion_patronal DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    bonificacion_ley DECIMAL(14,2) NOT NULL DEFAULT 0.00,

    fecha_inicio_vigencia DATE NOT NULL,
    fecha_fin_vigencia DATE NULL,
    motivo_cambio VARCHAR(255) NULL,
    observaciones VARCHAR(500) NULL,

    creado_por BIGINT UNSIGNED NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_historial_salarial_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES empleado (id_empleado)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_historial_salarial_usuario
        FOREIGN KEY (creado_por)
        REFERENCES usuario (id_usuario)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_historial_salarial_empleado (id_empleado),
    INDEX idx_historial_salarial_vigencia (fecha_inicio_vigencia, fecha_fin_vigencia)
) ENGINE=InnoDB;

-- ============================================================
-- 4. LOGROS Y DOCUMENTOS
-- ============================================================

CREATE TABLE logro (
    id_logro BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_tipo_logro BIGINT UNSIGNED NOT NULL,
    nombre VARCHAR(180) NOT NULL,
    descripcion TEXT NULL,
    institucion VARCHAR(180) NULL,
    fecha_obtencion DATE NULL,
    origen ENUM('KINAL','PROPIO','OTRO') NOT NULL DEFAULT 'OTRO',
    financiado_por_kinal BOOLEAN NOT NULL DEFAULT FALSE,
    detalle_financiacion TEXT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_logro_tipo
        FOREIGN KEY (id_tipo_logro)
        REFERENCES tipo_logro (id_tipo_logro)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_logro_tipo (id_tipo_logro),
    INDEX idx_logro_fecha (fecha_obtencion)
) ENGINE=InnoDB;

CREATE TABLE empleado_logro (
    id_empleado BIGINT UNSIGNED NOT NULL,
    id_logro BIGINT UNSIGNED NOT NULL,
    destacado BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones VARCHAR(500) NULL,

    PRIMARY KEY (id_empleado, id_logro),

    CONSTRAINT fk_empleado_logro_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES empleado (id_empleado)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_empleado_logro_logro
        FOREIGN KEY (id_logro)
        REFERENCES logro (id_logro)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE documento (
    id_documento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_empleado BIGINT UNSIGNED NOT NULL,
    id_tipo_documento BIGINT UNSIGNED NOT NULL,
    id_logro BIGINT UNSIGNED NULL,

    nombre_original VARCHAR(255) NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_storage VARCHAR(500) NOT NULL,
    mime_type VARCHAR(120) NULL,
    extension VARCHAR(20) NULL,
    tamano_bytes BIGINT UNSIGNED NULL,
    hash_archivo CHAR(64) NULL,
    fecha_documento DATE NULL,

    activo BOOLEAN NOT NULL DEFAULT TRUE,
    subido_por BIGINT UNSIGNED NULL,

    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_documento_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES empleado (id_empleado)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_documento_tipo
        FOREIGN KEY (id_tipo_documento)
        REFERENCES tipo_documento (id_tipo_documento)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_documento_logro
        FOREIGN KEY (id_logro)
        REFERENCES logro (id_logro)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_documento_usuario
        FOREIGN KEY (subido_por)
        REFERENCES usuario (id_usuario)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_documento_empleado (id_empleado),
    INDEX idx_documento_logro (id_logro),
    INDEX idx_documento_hash (hash_archivo)
) ENGINE=InnoDB;

-- ============================================================
-- 5. SOLICITUDES DE CAMBIO
-- ============================================================

CREATE TABLE solicitud_cambio (
    id_solicitud_cambio BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_empleado BIGINT UNSIGNED NOT NULL,

    tipo_cambio VARCHAR(80) NOT NULL,
    motivo TEXT NULL,

    estado ENUM(
        'PENDIENTE',
        'APROBADA_PARCIAL',
        'APROBADA',
        'RECHAZADA',
        'APLICADA',
        'CANCELADA'
    ) NOT NULL DEFAULT 'PENDIENTE',

    solicitado_por BIGINT UNSIGNED NOT NULL,
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion DATETIME NULL,
    resuelto_por BIGINT UNSIGNED NULL,
    observaciones VARCHAR(1000) NULL,

    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_solicitud_cambio_empleado
        FOREIGN KEY (id_empleado)
        REFERENCES empleado (id_empleado)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_solicitud_cambio_solicitante
        FOREIGN KEY (solicitado_por)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_solicitud_cambio_resolvedor
        FOREIGN KEY (resuelto_por)
        REFERENCES usuario (id_usuario)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_solicitud_cambio_empleado (id_empleado),
    INDEX idx_solicitud_cambio_estado (estado),
    INDEX idx_solicitud_cambio_fecha (fecha_solicitud)
) ENGINE=InnoDB;

CREATE TABLE detalle_cambio (
    id_detalle_cambio BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_solicitud_cambio BIGINT UNSIGNED NOT NULL,
    entidad VARCHAR(80) NOT NULL,
    registro_id BIGINT UNSIGNED NULL,
    campo VARCHAR(100) NOT NULL,
    valor_anterior TEXT NULL,
    valor_nuevo TEXT NULL,

    CONSTRAINT fk_detalle_cambio_solicitud
        FOREIGN KEY (id_solicitud_cambio)
        REFERENCES solicitud_cambio (id_solicitud_cambio)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_detalle_cambio_solicitud (id_solicitud_cambio)
) ENGINE=InnoDB;

CREATE TABLE aprobacion (
    id_aprobacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_solicitud_cambio BIGINT UNSIGNED NOT NULL,
    id_usuario BIGINT UNSIGNED NOT NULL,
    tipo_aprobador ENUM('GERENTE_AREA','RH') NOT NULL,
    estado ENUM('PENDIENTE','APROBADA','RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',
    comentario VARCHAR(1000) NULL,
    fecha_decision DATETIME NULL,

    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_aprobacion_solicitud
        FOREIGN KEY (id_solicitud_cambio)
        REFERENCES solicitud_cambio (id_solicitud_cambio)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_aprobacion_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_aprobacion_solicitud (id_solicitud_cambio),
    INDEX idx_aprobacion_usuario (id_usuario)
) ENGINE=InnoDB;

CREATE TABLE notificacion (
    id_notificacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT UNSIGNED NOT NULL,
    id_solicitud_cambio BIGINT UNSIGNED NULL,
    titulo VARCHAR(180) NOT NULL,
    mensaje TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_lectura DATETIME NULL,

    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_notificacion_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_notificacion_solicitud
        FOREIGN KEY (id_solicitud_cambio)
        REFERENCES solicitud_cambio (id_solicitud_cambio)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_notificacion_usuario (id_usuario, leida),
    INDEX idx_notificacion_solicitud (id_solicitud_cambio)
) ENGINE=InnoDB;

-- ============================================================
-- 6. AUDITORÍA
-- ============================================================

CREATE TABLE auditoria (
    id_auditoria BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT UNSIGNED NULL,

    accion VARCHAR(50) NOT NULL,
    entidad VARCHAR(80) NOT NULL,
    registro_id BIGINT UNSIGNED NULL,
    campo VARCHAR(100) NULL,
    valor_anterior TEXT NULL,
    valor_nuevo TEXT NULL,

    ip VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_auditoria_usuario (id_usuario),
    INDEX idx_auditoria_entidad (entidad, registro_id),
    INDEX idx_auditoria_fecha (fecha_hora),
    INDEX idx_auditoria_accion (accion)
) ENGINE=InnoDB;

-- ============================================================
-- 7. DATOS INICIALES DE CATÁLOGOS
-- ============================================================

INSERT INTO rol (nombre, descripcion) VALUES
('admin', 'Administración técnica, usuarios y configuración'),
('encargado', 'Gestión de Recursos Humanos'),
('gerenteArea', 'Consulta y gestión controlada del área'),
('gerenteGeneral', 'Consulta institucional y reportes especiales'),
('jefe', 'Consulta de colaboradores bajo responsabilidad'),
('visor', 'Consulta institucional para Imagen Institucional');

INSERT INTO nivel_academico (codigo, nombre, orden_nivel) VALUES
('PRIMARIA', 'Primaria', 10),
('DIVERSIFICADO', 'Diversificado', 20),
('TECNICO', 'Técnico', 30),
('ESP_TECNICA', 'Especialización técnica', 35),
('LICENCIATURA', 'Licenciatura', 40),
('ESPECIALIZACION', 'Especialización', 45),
('MAESTRIA', 'Maestría', 50),
('DOCTORADO', 'Doctorado', 60);

INSERT INTO estado_empleado
    (codigo, nombre, descripcion, es_estado_activo)
VALUES
('ACTIVO', 'Activo', 'Colaborador actualmente activo', TRUE),
('INACTIVO', 'Inactivo', 'Colaborador temporalmente no activo', FALSE),
('RETIRADO', 'Retirado', 'Colaborador que terminó su relación laboral', FALSE),
('SUSPENDIDO', 'Suspendido', 'Colaborador suspendido', FALSE),
('EN_PROCESO_CONTRATACION', 'En proceso de contratación', 'Proceso previo al alta definitiva', FALSE),
('FINALIZADO', 'Finalizado', 'Relación laboral finalizada', FALSE);

INSERT INTO tipo_contrato
    (codigo, nombre, descripcion, requiere_fecha_fin)
VALUES
('NOMINA', 'Empleado de nómina', 'Contrato principal de nómina', FALSE),
('TEMPORAL', 'Contrato temporal', 'Contrato con fecha de finalización', TRUE),
('SERVICIOS', 'Servicios profesionales', 'Contrato de servicios', TRUE),
('OTRO', 'Otro', 'Tipo por definir por RH', FALSE);

INSERT INTO tipo_logro
    (codigo, nombre, descripcion)
VALUES
('TITULO', 'Título', 'Título académico o profesional'),
('CERTIFICACION', 'Certificación', 'Certificación obtenida'),
('CURSO', 'Curso', 'Curso completado'),
('DIPLOMADO', 'Diplomado', 'Diplomado completado'),
('RECONOCIMIENTO', 'Reconocimiento', 'Reconocimiento institucional o externo'),
('PREMIO', 'Premio', 'Premio o distinción'),
('PUBLICACION', 'Publicación', 'Publicación o producción académica'),
('PARTICIPACION', 'Participación', 'Participación relevante'),
('OTRO', 'Otro', 'Otro tipo de logro');

INSERT INTO tipo_documento
    (codigo, nombre, descripcion)
VALUES
('DPI', 'Documento DPI', 'Imagen o archivo del DPI'),
('FOTOGRAFIA', 'Fotografía', 'Fotografía del colaborador'),
('LOGRO', 'Documento de logro', 'Documento de respaldo de un logro'),
('CONTRATO', 'Contrato', 'Documento contractual'),
('ACADEMICO', 'Documento académico', 'Título, diploma o constancia'),
('OTRO', 'Otro', 'Documento adicional');

INSERT INTO permiso (codigo, nombre, descripcion, modulo) VALUES
('EMPLEADO_VER', 'Consultar empleados', 'Consultar información de colaboradores', 'EMPLEADOS'),
('EMPLEADO_CREAR', 'Crear empleados', 'Crear nuevos colaboradores', 'EMPLEADOS'),
('EMPLEADO_EDITAR', 'Editar empleados', 'Modificar información de colaboradores', 'EMPLEADOS'),
('EMPLEADO_CAMBIAR_ESTADO', 'Cambiar estado', 'Cambiar el estado laboral', 'EMPLEADOS'),
('EMPLEADO_EXPORTAR', 'Exportar empleados', 'Exportar información de colaboradores', 'REPORTES'),
('AREA_GESTIONAR', 'Gestionar áreas', 'Administrar áreas', 'CONFIGURACION'),
('PUESTO_GESTIONAR', 'Gestionar puestos', 'Administrar puestos', 'CONFIGURACION'),
('NIVEL_ACADEMICO_GESTIONAR', 'Gestionar niveles académicos', 'Administrar niveles', 'CONFIGURACION'),
('LOGRO_GESTIONAR', 'Gestionar logros', 'Administrar logros y tipos de logro', 'LOGROS'),
('CONTRATO_VER', 'Consultar contratos', 'Consultar información contractual', 'CONTRATOS'),
('SALARIO_VER', 'Consultar salarios', 'Consultar información salarial', 'SALARIOS'),
('SALARIO_EDITAR', 'Editar salarios', 'Modificar información salarial', 'SALARIOS'),
('CAMBIO_CREAR', 'Crear solicitud de cambio', 'Crear solicitudes', 'CAMBIOS'),
('CAMBIO_APROBAR_RH', 'Aprobar cambios RH', 'Aprobar cambios como RH', 'CAMBIOS'),
('CAMBIO_APROBAR_GERENCIA', 'Aprobar cambios gerencia', 'Aprobar cambios como gerente', 'CAMBIOS'),
('AUDITORIA_VER', 'Consultar auditoría', 'Consultar trazabilidad', 'AUDITORIA'),
('USUARIO_GESTIONAR', 'Gestionar usuarios', 'Administrar cuentas de usuario', 'SEGURIDAD'),
('ROL_GESTIONAR', 'Gestionar roles', 'Administrar roles y permisos', 'SEGURIDAD'),
('IMPORTACION_EJECUTAR', 'Importar XLSX', 'Ejecutar importaciones validadas', 'IMPORTACION'),
('REPORTES_PDF', 'Exportar PDF', 'Generar reportes PDF', 'REPORTES'),
('REPORTES_XLSX', 'Exportar XLSX', 'Generar reportes XLSX', 'REPORTES');

-- ============================================================
-- 8. VISTAS INICIALES
-- ============================================================

CREATE OR REPLACE VIEW vw_empleados_activos AS
SELECT
    e.id_empleado,
    e.dpi,
    e.nit,
    e.codigo_empleado,
    e.nombre_completo,
    e.fecha_nacimiento,
    e.fecha_ingreso_kinal,
    ea.id_area AS id_area_principal,
    a.nombre AS area_principal,
    e.id_puesto_actual,
    p.nombre AS puesto_actual,
    e.id_estado_empleado,
    ee.nombre AS estado_empleado
FROM empleado e
JOIN estado_empleado ee
    ON ee.id_estado_empleado = e.id_estado_empleado
LEFT JOIN area a
    ON a.id_area = e.id_area_principal
LEFT JOIN puesto p
    ON p.id_puesto = e.id_puesto_actual
LEFT JOIN empleado_area ea
    ON ea.id_empleado = e.id_empleado
   AND ea.id_area = e.id_area_principal
WHERE ee.es_estado_activo = TRUE;

CREATE OR REPLACE VIEW vw_empleados_por_area AS
SELECT
    e.id_empleado,
    e.dpi,
    e.nombre_completo,
    a.id_area,
    a.codigo AS codigo_area,
    a.nombre AS area,
    ea.es_principal,
    ee.nombre AS estado_empleado
FROM empleado e
JOIN empleado_area ea
    ON ea.id_empleado = e.id_empleado
JOIN area a
    ON a.id_area = ea.id_area
JOIN estado_empleado ee
    ON ee.id_estado_empleado = e.id_estado_empleado;

CREATE OR REPLACE VIEW vw_empleados_por_puesto AS
SELECT
    e.id_empleado,
    e.dpi,
    e.nombre_completo,
    p.id_puesto,
    p.codigo AS codigo_puesto,
    p.nombre AS puesto,
    a.id_area AS id_area_principal,
    a.nombre AS area_principal,
    ee.nombre AS estado_empleado
FROM empleado e
LEFT JOIN puesto p
    ON p.id_puesto = e.id_puesto_actual
LEFT JOIN area a
    ON a.id_area = e.id_area_principal
JOIN estado_empleado ee
    ON ee.id_estado_empleado = e.id_estado_empleado;

CREATE OR REPLACE VIEW vw_empleados_por_nivel_academico AS
SELECT
    e.id_empleado,
    e.dpi,
    e.nombre_completo,
    na.id_nivel_academico,
    na.nombre AS nivel_academico,
    ee.nombre AS estado_empleado
FROM empleado e
LEFT JOIN nivel_academico na
    ON na.id_nivel_academico = e.id_nivel_academico
JOIN estado_empleado ee
    ON ee.id_estado_empleado = e.id_estado_empleado;

CREATE OR REPLACE VIEW vw_logros_por_empleado AS
SELECT
    e.id_empleado,
    e.dpi,
    e.nombre_completo,
    l.id_logro,
    tl.nombre AS tipo_logro,
    l.nombre AS logro,
    l.institucion,
    l.fecha_obtencion,
    l.origen,
    l.financiado_por_kinal,
    el.destacado
FROM empleado e
JOIN empleado_logro el
    ON el.id_empleado = e.id_empleado
JOIN logro l
    ON l.id_logro = el.id_logro
JOIN tipo_logro tl
    ON tl.id_tipo_logro = l.id_tipo_logro
WHERE l.activo = TRUE;

-- ============================================================
-- 9. NOTAS DE IMPLEMENTACIÓN
-- ============================================================

-- 1) La integridad "un solo area principal vigente por empleado"
--    deberá validarse en la capa de aplicación o mediante trigger.
--
-- 2) La vigencia de contratos y salarios (sin solapamientos)
--    deberá validarse en la capa de servicio/transacción.
--
-- 3) No se almacenará edad; debe calcularse.
--
-- 4) Los archivos se almacenan fuera de la BD y se guarda la ruta
--    junto con metadatos.
--
-- 5) Las contraseñas nunca deben almacenarse en texto plano.
--
-- 6) El script NO crea usuarios MySQL de aplicación. La aplicación
--    debe conectarse con una cuenta con privilegios mínimos.
--
-- 7) El flujo de aprobación debe garantizar que las solicitudes
--    requieran las aprobaciones configuradas por RH/TIC antes de
--    aplicar el cambio.