package cl.ipss.kanban.mapper;

import cl.ipss.kanban.dto.ActualizarTareaRequest;
import cl.ipss.kanban.dto.CrearTareaRequest;
import cl.ipss.kanban.dto.TareaResponse;
import cl.ipss.kanban.entity.Tarea;
import cl.ipss.kanban.enums.EstadoTarea;
import org.springframework.stereotype.Component;

@Component
public class TareaMapper {


    public Tarea aEntidad(CrearTareaRequest r) {
        Tarea entidad = new Tarea();
        entidad.setTitulo(r.titulo().trim());
        entidad.setDescripcion(limpiar(r.descripcion()));
        entidad.setEstado(EstadoTarea.POR_HACER);
        entidad.setPrioridad(r.prioridad());
        entidad.setFechaLimite(r.fechaLimite());
        return entidad;
    }

    public void actualizarEntidad(Tarea entidad, ActualizarTareaRequest r) {
        entidad.setTitulo(r.titulo().trim());
        entidad.setDescripcion(limpiar(r.descripcion()));
        entidad.setPrioridad(r.prioridad());
        entidad.setFechaLimite(r.fechaLimite());
    }

    public TareaResponse aRespuesta(Tarea t) {
        return new TareaResponse(t.getId(), t.getTitulo(), t.getDescripcion(), t.getEstado(),
                t.getPrioridad(), t.getFechaLimite(), t.getCreadaEn(), t.getActualizadaEn());
    }

    private String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
