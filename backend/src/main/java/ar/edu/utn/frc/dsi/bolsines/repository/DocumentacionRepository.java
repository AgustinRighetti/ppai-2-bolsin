package ar.edu.utn.frc.dsi.bolsines.repository;

import ar.edu.utn.frc.dsi.bolsines.entity.Documentacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentacionRepository extends JpaRepository<Documentacion, Long> {
}
