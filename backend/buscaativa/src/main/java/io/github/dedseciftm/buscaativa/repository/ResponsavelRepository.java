package io.github.dedseciftm.buscaativa.repository;

import java.util.List;
import java.util.UUID;

import io.github.dedseciftm.buscaativa.model.Responsavel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResponsavelRepository extends JpaRepository<Responsavel, UUID> {

	/** Quantidade de responsáveis de um aluno; alunos sem responsável não aparecem nas contagens agrupadas. */
	interface ContagemPorAluno {

		String getIdDoAluno();

		long getTotal();

	}

	List<Responsavel> findAllByOrderByNomeAsc();

	List<Responsavel> findAllByAlunoIdDoAlunoOrderByNomeAsc(String idDoAluno);

	long countByAlunoIdDoAluno(String idDoAluno);

	// Uma consulta agrupada no lugar de uma contagem por aluno: com milhares de alunos, a diferença é enorme.
	@Query("select r.aluno.idDoAluno as idDoAluno, count(r) as total from Responsavel r group by r.aluno.idDoAluno")
	List<ContagemPorAluno> contarPorAluno();

	@Query("""
			select r.aluno.idDoAluno as idDoAluno, count(r) as total from Responsavel r
			where r.aluno.turma.idTurma = :idTurma group by r.aluno.idDoAluno""")
	List<ContagemPorAluno> contarPorAlunoDaTurma(@Param("idTurma") String idTurma);

}
