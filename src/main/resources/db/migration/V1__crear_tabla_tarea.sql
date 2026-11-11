-- V1: tabla principal del tablero
CREATE TABLE tarea (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titulo         VARCHAR(100) NOT NULL,
    descripcion    VARCHAR(500),
    estado         VARCHAR(15)  NOT NULL DEFAULT 'POR_HACER',
    prioridad      VARCHAR(10)  NOT NULL DEFAULT 'MEDIA',
    fecha_limite   DATE,
    creada_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizada_en TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_tarea_estado    CHECK (estado IN ('POR_HACER', 'EN_CURSO', 'HECHA')),
    CONSTRAINT ck_tarea_prioridad CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA'))
);

CREATE INDEX idx_tarea_estado ON tarea (estado);
