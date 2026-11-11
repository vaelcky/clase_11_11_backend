package cl.ipss.kanban.exception;

/** Se lanza al editar una tarea que ya está HECHA (HTTP 409). */
public class TareaHechaNoEditableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TareaHechaNoEditableException(Long id) {
        super("La tarea " + id + " está hecha y no se puede editar; muévela a En curso para modificarla");
    }
}
