package cl.ipss.kanban.service;

import cl.ipss.kanban.dto.ActualizarTareaRequest;
import cl.ipss.kanban.dto.CrearTareaRequest;
import cl.ipss.kanban.dto.TareaResponse;
import cl.ipss.kanban.entity.Tarea;
import cl.ipss.kanban.enums.EstadoTarea;
import cl.ipss.kanban.enums.PrioridadTarea;
import cl.ipss.kanban.exception.LimiteEnCursoExcedidoException;
import cl.ipss.kanban.exception.MovimientoInvalidoException;
import cl.ipss.kanban.exception.TareaHechaNoEditableException;
import cl.ipss.kanban.exception.TareaNoEncontradaException;
import cl.ipss.kanban.mapper.TareaMapper;
import cl.ipss.kanban.repository.TareaRepository;
import cl.ipss.kanban.service.impl.TareaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario del service: el repository se "simula" con Mockito,
 * así no hace falta levantar Spring ni PostgreSQL.
 */
@ExtendWith(MockitoExtension.class)
class TareaServiceImplTest {

    @Mock
    private TareaRepository repository;

    private TareaServiceImpl service;

    @BeforeEach
    void preparar() {
        service = new TareaServiceImpl(repository, new TareaMapper());
    }

    private Tarea tarea(EstadoTarea estado) {
        Tarea t = new Tarea();
        t.setTitulo("Tarea de prueba");
        t.setPrioridad(PrioridadTarea.MEDIA);
        t.setEstado(estado);
        return t;
    }

    @Test
    void creaTareaLimpiandoDatosYNaceEnPorHacer() {
        when(repository.save(any(Tarea.class))).thenAnswer(inv -> {
            Tarea t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 1L); // la BD real asigna el id
            return t;
        });

        TareaResponse r = service.crear(new CrearTareaRequest(" Estudiar ", "  ", PrioridadTarea.ALTA, null));

        assertEquals(1L, r.id());
        assertEquals("Estudiar", r.titulo());
        assertNull(r.descripcion());
        assertEquals(EstadoTarea.POR_HACER, r.estado());
    }

    @Test
    void obtenerLanzaErrorSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TareaNoEncontradaException.class, () -> service.obtener(99L));
    }

    @Test
    void actualizaDatosSinCambiarElEstado() {
        Tarea existente = tarea(EstadoTarea.EN_CURSO);
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        TareaResponse r = service.actualizar(5L,
                new ActualizarTareaRequest("Nuevo título", "Detalle", PrioridadTarea.BAJA, null));

        assertEquals("Nuevo título", r.titulo());
        assertEquals(PrioridadTarea.BAJA, r.prioridad());
        assertEquals(EstadoTarea.EN_CURSO, r.estado());
    }

    @Test
    void noEditaUnaTareaHecha() {
        when(repository.findById(5L)).thenReturn(Optional.of(tarea(EstadoTarea.HECHA)));

        assertThrows(TareaHechaNoEditableException.class, () -> service.actualizar(5L,
                new ActualizarTareaRequest("Otro", null, PrioridadTarea.BAJA, null)));
        verify(repository, never()).save(any());
    }

    @Test
    void mueveDePorHacerAEnCurso() {
        Tarea t = tarea(EstadoTarea.POR_HACER);
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(repository.countByEstado(EstadoTarea.EN_CURSO)).thenReturn(2L);
        when(repository.save(t)).thenReturn(t);

        TareaResponse r = service.mover(1L, EstadoTarea.EN_CURSO);

        assertEquals(EstadoTarea.EN_CURSO, r.estado());
    }

    @Test
    void rechazaLaCuartaTareaEnCurso() {
        when(repository.findById(1L)).thenReturn(Optional.of(tarea(EstadoTarea.POR_HACER)));
        when(repository.countByEstado(EstadoTarea.EN_CURSO)).thenReturn(3L);

        assertThrows(LimiteEnCursoExcedidoException.class, () -> service.mover(1L, EstadoTarea.EN_CURSO));
        verify(repository, never()).save(any());
    }

    @Test
    void rechazaSaltarDePorHacerAHecha() {
        when(repository.findById(1L)).thenReturn(Optional.of(tarea(EstadoTarea.POR_HACER)));

        assertThrows(MovimientoInvalidoException.class, () -> service.mover(1L, EstadoTarea.HECHA));
        verify(repository, never()).save(any());
    }

    @Test
    void rechazaMoverALaMismaColumna() {
        when(repository.findById(1L)).thenReturn(Optional.of(tarea(EstadoTarea.EN_CURSO)));

        assertThrows(MovimientoInvalidoException.class, () -> service.mover(1L, EstadoTarea.EN_CURSO));
    }

    @Test
    void permiteRetrocederDeHechaAEnCursoSiHayCupo() {
        Tarea t = tarea(EstadoTarea.HECHA);
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(repository.countByEstado(EstadoTarea.EN_CURSO)).thenReturn(1L);
        when(repository.save(t)).thenReturn(t);

        assertEquals(EstadoTarea.EN_CURSO, service.mover(1L, EstadoTarea.EN_CURSO).estado());
    }

    @Test
    void retrocederAPorHacerNoRevisaElLimite() {
        Tarea t = tarea(EstadoTarea.EN_CURSO);
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(repository.save(t)).thenReturn(t);

        assertEquals(EstadoTarea.POR_HACER, service.mover(1L, EstadoTarea.POR_HACER).estado());
        verify(repository, never()).countByEstado(any());
    }

    @Test
    void moverLanzaErrorSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TareaNoEncontradaException.class, () -> service.mover(99L, EstadoTarea.EN_CURSO));
    }

    @Test
    void eliminaSoloSiExiste() {
        when(repository.existsById(7L)).thenReturn(true);
        when(repository.existsById(8L)).thenReturn(false);

        service.eliminar(7L);

        verify(repository).deleteById(7L);
        assertThrows(TareaNoEncontradaException.class, () -> service.eliminar(8L));
    }
}
