package io.github.dedseciftm.buscaativa.repository;

import java.util.List;

import io.github.dedseciftm.buscaativa.model.Aluno;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, String> {

	// A turma vai na mesma consulta; sem isso, cada turma distinta seria buscada à parte.
	@EntityGraph(attributePaths = "turma")
	List<Aluno> findAllByOrderByNomeAsc();

	@EntityGraph(attributePaths = "turma")
	List<Aluno> findAllByTurmaIdTurmaOrderByNomeAsc(String idTurma);

}
