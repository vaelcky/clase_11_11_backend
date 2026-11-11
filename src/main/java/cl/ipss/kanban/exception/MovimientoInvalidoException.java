package cl.ipss.kanban.exception;

import cl.ipss.kanban.enums.EstadoTarea;

/** Se lanza al mover una tarea a una columna que no es contigua a la actual (HTTP 409). */
public class MovimientoInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MovimientoInvalidoException(EstadoTarea origen, EstadoTarea destino) {
        super("No se puede mover una tarea de " + origen + " a " + destino
                + "; solo se avanza o retrocede una columna a la vez");
    }
}
