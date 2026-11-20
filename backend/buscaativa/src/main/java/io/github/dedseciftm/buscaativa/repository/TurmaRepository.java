package io.github.dedseciftm.buscaativa.repository;

import java.util.List;

import io.github.dedseciftm.buscaativa.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurmaRepository extends JpaRepository<Turma, String> {

	List<Turma> findAllByOrderByIdTurmaAsc();

}
