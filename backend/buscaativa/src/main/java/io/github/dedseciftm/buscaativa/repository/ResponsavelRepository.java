package io.github.dedseciftm.buscaativa.repository;

import java.util.List;
import java.util.UUID;

import io.github.dedseciftm.buscaativa.model.Responsavel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponsavelRepository extends JpaRepository<Responsavel, UUID> {

	List<Responsavel> findAllByOrderByNomeAsc();

	List<Responsavel> findAllByAlunoIdDoAlunoOrderByNomeAsc(String idDoAluno);

	long countByAlunoIdDoAluno(String idDoAluno);

}
