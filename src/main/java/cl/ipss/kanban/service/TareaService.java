package cl.ipss.kanban.service;

import cl.ipss.kanban.dto.ActualizarTareaRequest;
import cl.ipss.kanban.dto.CrearTareaRequest;
import cl.ipss.kanban.dto.TareaResponse;
import cl.ipss.kanban.enums.EstadoTarea;

import java.util.List;

/**
 * SERVICE (interfaz): define QUÉ operaciones de negocio existen.
 * El controller depende de esta interfaz; la lógica está en TareaServiceImpl.
 */
public interface TareaService {

    List<TareaResponse> listar();

    TareaResponse obtener(Long id);

    TareaResponse crear(CrearTareaRequest request);

    TareaResponse actualizar(Long id, ActualizarTareaRequest request);

    /** Mueve la tarea a otra columna aplicando las reglas del tablero. */
    TareaResponse mover(Long id, EstadoTarea destino);

    void eliminar(Long id);
}
