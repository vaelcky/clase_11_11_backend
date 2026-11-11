package cl.ipss.kanban.controller;

import cl.ipss.kanban.dto.TareaResponse;
import cl.ipss.kanban.enums.EstadoTarea;
import cl.ipss.kanban.enums.PrioridadTarea;
import cl.ipss.kanban.exception.LimiteEnCursoExcedidoException;
import cl.ipss.kanban.exception.MovimientoInvalidoException;
import cl.ipss.kanban.exception.TareaNoEncontradaException;
import cl.ipss.kanban.service.TareaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba solo la capa web (rutas, validaciones y códigos HTTP) con el service simulado. */
@WebMvcTest(TareaController.class)
class TareaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TareaService service;

    private static final String JSON_VALIDO = "{\"titulo\":\"Estudiar\",\"prioridad\":\"ALTA\"}";

    private TareaResponse ejemplo(EstadoTarea estado) {
        LocalDateTime ahora = LocalDateTime.now();
        return new TareaResponse(1L, "Estudiar", null, estado, PrioridadTarea.ALTA, null, ahora, ahora);
    }

    @Test
    void listaTareas() throws Exception {
        when(service.listar()).thenReturn(List.of(ejemplo(EstadoTarea.POR_HACER)));

        mvc.perform(get("/api/tareas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Estudiar"));
    }

    @Test
    void creaTareaDevuelve201ConLocation() throws Exception {
        when(service.crear(any())).thenReturn(ejemplo(EstadoTarea.POR_HACER));

        mvc.perform(post("/api/tareas").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tareas/1"))
                .andExpect(jsonPath("$.estado").value("POR_HACER"));
    }

    @Test
    void tituloVacioDevuelve400ConDetalle() throws Exception {
        String json = "{\"titulo\":\"\",\"prioridad\":\"ALTA\"}";

        mvc.perform(post("/api/tareas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value("titulo: El título es obligatorio"));
    }

    @Test
    void tareaInexistenteDevuelve404() throws Exception {
        when(service.obtener(99L)).thenThrow(new TareaNoEncontradaException(99L));

        mvc.perform(get("/api/tareas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe una tarea con id 99"));
    }

    @Test
    void mueveTareaDevuelve200() throws Exception {
        when(service.mover(eq(1L), eq(EstadoTarea.EN_CURSO))).thenReturn(ejemplo(EstadoTarea.EN_CURSO));

        mvc.perform(patch("/api/tareas/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_CURSO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));
    }

    @Test
    void limiteEnCursoDevuelve409() throws Exception {
        when(service.mover(eq(1L), eq(EstadoTarea.EN_CURSO))).thenThrow(new LimiteEnCursoExcedidoException(3));

        mvc.perform(patch("/api/tareas/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_CURSO\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void movimientoInvalidoDevuelve409() throws Exception {
        when(service.mover(eq(1L), eq(EstadoTarea.HECHA)))
                .thenThrow(new MovimientoInvalidoException(EstadoTarea.POR_HACER, EstadoTarea.HECHA));

        mvc.perform(patch("/api/tareas/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"HECHA\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void estadoDesconocidoDevuelve400() throws Exception {
        mvc.perform(patch("/api/tareas/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ARCHIVADA\"}"))
                .andExpect(status().isBadRequest());
    }
}
