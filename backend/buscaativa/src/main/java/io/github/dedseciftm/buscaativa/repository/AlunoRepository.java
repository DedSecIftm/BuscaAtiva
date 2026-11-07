package io.github.dedseciftm.buscaativa.repository;

import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.model.Turma;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {

    @EntityGraph(attributePaths = "turma")
    List<Aluno> findByExcluidoEmIsNullOrderByNomeAsc();

    @EntityGraph(attributePaths = "turma")
    Optional<Aluno> findByIdAndExcluidoEmIsNull(UUID id);

    // As buscas abaixo incluem alunos excluídos: a matrícula continua reservada e o aluno pode voltar.
    Optional<Aluno> findByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, UUID id);

    List<Aluno> findByMatriculaIn(Collection<String> matriculas);

    List<Aluno> findByTurmaIn(Collection<Turma> turmas);

}
