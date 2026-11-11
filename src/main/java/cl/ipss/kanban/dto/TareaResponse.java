package cl.ipss.kanban.dto;

import cl.ipss.kanban.enums.EstadoTarea;
import cl.ipss.kanban.enums.PrioridadTarea;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TareaResponse(
        Long id,
        String titulo,
        String descripcion,
        EstadoTarea estado,
        PrioridadTarea prioridad,
        LocalDate fechaLimite,
        LocalDateTime creadaEn,
        LocalDateTime actualizadaEn
) {
}
