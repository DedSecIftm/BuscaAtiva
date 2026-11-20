package io.github.dedseciftm.buscaativa.repository;

import java.util.List;

import io.github.dedseciftm.buscaativa.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, String> {

	List<Aluno> findAllByOrderByNomeAsc();

	List<Aluno> findAllByTurmaIdTurmaOrderByNomeAsc(String idTurma);

}
