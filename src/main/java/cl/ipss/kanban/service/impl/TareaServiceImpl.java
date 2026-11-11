package cl.ipss.kanban.service.impl;

import cl.ipss.kanban.dto.ActualizarTareaRequest;
import cl.ipss.kanban.dto.CrearTareaRequest;
import cl.ipss.kanban.dto.TareaResponse;
import cl.ipss.kanban.entity.Tarea;
import cl.ipss.kanban.enums.EstadoTarea;
import cl.ipss.kanban.exception.LimiteEnCursoExcedidoException;
import cl.ipss.kanban.exception.MovimientoInvalidoException;
import cl.ipss.kanban.exception.TareaHechaNoEditableException;
import cl.ipss.kanban.exception.TareaNoEncontradaException;
import cl.ipss.kanban.mapper.TareaMapper;
import cl.ipss.kanban.repository.TareaRepository;
import cl.ipss.kanban.service.TareaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SERVICE (implementación): aquí viven las reglas de negocio del tablero.
 * Recibe el repository y el mapper por el constructor (inyección de dependencias).
 */
@Service
@Transactional
public class TareaServiceImpl implements TareaService {

    private static final Logger log = LoggerFactory.getLogger(TareaServiceImpl.class);

    /** Máximo de tareas que pueden estar a la vez en la columna EN_CURSO. */
    static final int LIMITE_EN_CURSO = 3;

    private final TareaRepository repository;
    private final TareaMapper mapper;

    public TareaServiceImpl(TareaRepository repository, TareaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaResponse> listar() {
        log.info("--- command receive to list tasks ---");
        List<TareaResponse> tareas = repository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(mapper::aRespuesta)
                .toList();
        log.info("--- finish list tasks: {} found ---", tareas.size());
        return tareas;
    }

    @Override
    @Transactional(readOnly = true)
    public TareaResponse obtener(Long id) {
        log.info("--- command receive to get task: {} ---", id);
        TareaResponse tarea = mapper.aRespuesta(buscar(id));
        log.info("--- finish get task: {} - {} ---", id, tarea.titulo());
        return tarea;
    }

    @Override
    public TareaResponse crear(CrearTareaRequest request) {
        log.info("--- command receive to create task ---");
        Tarea guardada = repository.save(mapper.aEntidad(request));
        log.info("--- finish create task: id {} ---", guardada.getId());
        return mapper.aRespuesta(guardada);
    }

    @Override
    public TareaResponse actualizar(Long id, ActualizarTareaRequest request) {
        log.info("--- command receive to update task: {} ---", id);
        Tarea tarea = buscar(id);
        if (tarea.getEstado() == EstadoTarea.HECHA) {
            log.warn("--- done task cannot be edited: {} ---", id);
            throw new TareaHechaNoEditableException(id);
        }
        mapper.actualizarEntidad(tarea, request);
        Tarea guardada = repository.save(tarea);
        log.info("--- finish update task: {} ---", id);
        return mapper.aRespuesta(guardada);
    }

    @Override
    public TareaResponse mover(Long id, EstadoTarea destino) {
        log.info("--- command receive to move task: {} to {} ---", id, destino);
        Tarea tarea = buscar(id);
        EstadoTarea origen = tarea.getEstado();

        // Regla 2: solo se avanza o retrocede UNA columna (POR_HACER <-> EN_CURSO <-> HECHA).
        if (Math.abs(destino.ordinal() - origen.ordinal()) != 1) {
            log.warn("--- invalid move: task {} from {} to {} ---", id, origen, destino);
            throw new MovimientoInvalidoException(origen, destino);
        }
        // Regla 1: máximo 3 tareas trabajándose a la vez.
        if (destino == EstadoTarea.EN_CURSO && repository.countByEstado(EstadoTarea.EN_CURSO) >= LIMITE_EN_CURSO) {
            log.warn("--- in-progress limit reached: task {} cannot move to {} ---", id, destino);
            throw new LimiteEnCursoExcedidoException(LIMITE_EN_CURSO);
        }

        tarea.setEstado(destino);
        Tarea guardada = repository.save(tarea);
        log.info("--- finish move task: {} from {} to {} ---", id, origen, destino);
        return mapper.aRespuesta(guardada);
    }

    @Override
    public void eliminar(Long id) {
        log.info("--- command receive to delete task: {} ---", id);
        if (!repository.existsById(id)) {
            log.warn("--- task not found: {} ---", id);
            throw new TareaNoEncontradaException(id);
        }
        repository.deleteById(id);
        log.info("--- finish delete task: {} ---", id);
    }

    private Tarea buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> {
            log.warn("--- task not found: {} ---", id);
            return new TareaNoEncontradaException(id);
        });
    }
}
