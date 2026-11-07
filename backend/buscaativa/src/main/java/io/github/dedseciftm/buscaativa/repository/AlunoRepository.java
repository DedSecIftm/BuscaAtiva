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

    @EntityGraph(attributePaths = {"turma", "responsaveis.responsavel"})
    List<Aluno> findByExcluidoEmIsNullOrderByNomeAsc();

    @EntityGraph(attributePaths = {"turma", "responsaveis.responsavel"})
    Optional<Aluno> findByIdAndExcluidoEmIsNull(UUID id);

    // As buscas abaixo incluem alunos excluídos: a matrícula continua reservada e o aluno pode voltar.
    Optional<Aluno> findByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, UUID id);

    // Com os responsáveis já carregados, a importação não faz uma consulta por aluno ao vincular.
    @EntityGraph(attributePaths = "responsaveis")
    List<Aluno> findByMatriculaIn(Collection<String> matriculas);

    @EntityGraph(attributePaths = "responsaveis")
    List<Aluno> findByTurmaIn(Collection<Turma> turmas);

}
