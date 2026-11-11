package cl.ipss.kanban.exception;

/** Se lanza cuando no existe la tarea pedida (HTTP 404). */
public class TareaNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TareaNoEncontradaException(Long id) {
        super("No existe una tarea con id " + id);
    }
}
