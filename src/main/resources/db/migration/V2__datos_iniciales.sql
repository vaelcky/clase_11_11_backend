-- V2: tres tareas de ejemplo, una por columna, para ver algo en el tablero al levantar el proyecto
INSERT INTO tarea (titulo, descripcion, estado, prioridad, fecha_limite) VALUES
    ('Preparar la presentación',      'Armar las diapositivas de la clase del viernes', 'POR_HACER', 'MEDIA', NULL),
    ('Estudiar para la prueba de MVC', 'Repasar capas, DTO y patrones de diseño',       'EN_CURSO',  'ALTA',  CURRENT_DATE + 7),
    ('Instalar PostgreSQL 18',         'Crear la base kanban_db y el usuario kanban',   'HECHA',     'BAJA',  NULL);
