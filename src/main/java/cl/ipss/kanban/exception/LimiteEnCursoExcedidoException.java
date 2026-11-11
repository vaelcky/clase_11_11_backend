package cl.ipss.kanban.exception;

/** Se lanza al mover una tarea a EN_CURSO cuando esa columna ya está llena (HTTP 409). */
public class LimiteEnCursoExcedidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LimiteEnCursoExcedidoException(int limite) {
        super("La columna En curso ya tiene el máximo de " + limite
                + " tareas; termina una antes de iniciar otra");
    }
}
