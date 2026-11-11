package cl.ipss.kanban.repository;

import cl.ipss.kanban.entity.Tarea;
import cl.ipss.kanban.enums.EstadoTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * REPOSITORY: acceso a datos. Al extender JpaRepository, Spring genera
 * en tiempo de ejecución save, findById, findAll, deleteById, existsById...
 */
@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    /** Método derivado: Spring crea el SELECT COUNT leyendo el nombre del método. */
    long countByEstado(EstadoTarea estado);
}
