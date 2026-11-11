package cl.ipss.kanban.dto;

import cl.ipss.kanban.enums.PrioridadTarea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
public record ActualizarTareaRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 100, message = "El título no puede superar los 100 caracteres")
        String titulo,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @NotNull(message = "La prioridad es obligatoria")
        PrioridadTarea prioridad,

        LocalDate fechaLimite
) {
}
