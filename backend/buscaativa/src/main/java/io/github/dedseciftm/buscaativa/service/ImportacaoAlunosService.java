package io.github.dedseciftm.buscaativa.service;

import io.github.dedseciftm.buscaativa.dto.ImportacaoAlunosRequest.Linha;
import io.github.dedseciftm.buscaativa.dto.ImportacaoAlunosResponse;
import io.github.dedseciftm.buscaativa.dto.ImportacaoAlunosResponse.ErroDeLinha;
import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.model.Turma;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.TurmaRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Grava os alunos de um arquivo importado. Reimportar um arquivo atualiza os alunos em vez de duplicá-los:
 * o aluno é reconhecido pela matrícula ou, nos arquivos sem matrícula, pelo nome dentro da turma.
 * Campos que não vieram no arquivo mantêm o valor atual, e um aluno excluído que reaparece volta a valer.
 */
@Service
public class ImportacaoAlunosService {

    private final AlunoRepository repository;
    private final TurmaRepository turmaRepository;
    private final Validator validator;

    public ImportacaoAlunosService(AlunoRepository repository, TurmaRepository turmaRepository, Validator validator) {
        this.repository = repository;
        this.turmaRepository = turmaRepository;
        this.validator = validator;
    }

    @Transactional
    public ImportacaoAlunosResponse importar(List<Linha> linhas) {
        var erros = new ArrayList<ErroDeLinha>();
        var validas = new ArrayList<Linha>();
        for (int i = 0; i < linhas.size(); i++) {
            var violacoes = validator.validate(linhas.get(i));
            if (violacoes.isEmpty()) {
                validas.add(linhas.get(i));
            } else {
                erros.add(new ErroDeLinha(i + 1, violacoes.stream().map(ConstraintViolation::getMessage)
                        .sorted().collect(Collectors.joining(" "))));
            }
        }

        var turmas = turmasDe(validas);
        // Buscados de uma vez para não fazer uma consulta por registro em arquivos grandes.
        var porMatricula = new HashMap<>(repository.findByMatriculaIn(matriculasDe(validas)).stream()
                .collect(Collectors.toMap(Aluno::getMatricula, Function.identity())));
        var porNomeNaTurma = new HashMap<String, Aluno>();
        repository.findByTurmaIn(turmas.values())
                .forEach((aluno) -> porNomeNaTurma.putIfAbsent(chave(aluno.getTurma(), aluno.getNome()), aluno));

        int criados = 0;
        int atualizados = 0;
        for (var linha : validas) {
            var turma = turmas.get(normalizar(linha.turma()));
            var matricula = textoOuNulo(linha.matricula());
            var chaveDoAluno = chave(turma, linha.nome());

            var aluno = localizar(matricula, porMatricula, porNomeNaTurma.get(chaveDoAluno));
            if (aluno == null) {
                aluno = new Aluno();
                criados++;
            } else {
                atualizados++;
            }
            preencher(aluno, linha, turma);
            repository.save(aluno);

            porNomeNaTurma.putIfAbsent(chaveDoAluno, aluno);
            if (matricula != null) {
                porMatricula.put(matricula, aluno);
            }
        }
        return new ImportacaoAlunosResponse(criados, atualizados, erros);
    }

    /** Quem já tem outra matrícula é outra pessoa, mesmo com o mesmo nome na mesma turma. */
    private static Aluno localizar(String matricula, Map<String, Aluno> porMatricula, Aluno mesmoNomeNaTurma) {
        if (matricula == null) {
            return mesmoNomeNaTurma;
        }
        var aluno = porMatricula.get(matricula);
        if (aluno != null) {
            return aluno;
        }
        return mesmoNomeNaTurma != null && mesmoNomeNaTurma.getMatricula() == null ? mesmoNomeNaTurma : null;
    }

    private static void preencher(Aluno aluno, Linha linha, Turma turma) {
        aluno.setNome(linha.nome().trim());
        aluno.setTurma(turma);
        aluno.setExcluidoEm(null);
        if (textoOuNulo(linha.matricula()) != null) {
            aluno.setMatricula(linha.matricula().trim());
        }
        if (textoOuNulo(linha.email()) != null) {
            aluno.setEmail(linha.email().trim());
        }
        if (textoOuNulo(linha.telefone()) != null) {
            aluno.setTelefone(linha.telefone().trim());
        }
        if (textoOuNulo(linha.faltas()) != null) {
            aluno.setFaltas(Integer.valueOf(linha.faltas()));
        }
        if (textoOuNulo(linha.periodoEmDias()) != null) {
            aluno.setPeriodoEmDias(Integer.valueOf(linha.periodoEmDias()));
        }
    }

    /** Turmas usadas no arquivo pelo nome normalizado; as que ainda não existem são criadas. */
    private Map<String, Turma> turmasDe(List<Linha> linhas) {
        var existentes = turmaRepository.findAll().stream()
                .collect(Collectors.toMap((turma) -> normalizar(turma.getNome()), Function.identity()));
        var turmas = new HashMap<String, Turma>();
        for (var linha : linhas) {
            turmas.computeIfAbsent(normalizar(linha.turma()), (chave) -> existentes.containsKey(chave)
                    ? existentes.get(chave)
                    : turmaRepository.save(new Turma(linha.turma().trim())));
        }
        return turmas;
    }

    private static List<String> matriculasDe(List<Linha> linhas) {
        return linhas.stream().map(Linha::matricula).map(ImportacaoAlunosService::textoOuNulo)
                .filter((matricula) -> matricula != null).distinct().toList();
    }

    private static String chave(Turma turma, String nome) {
        return turma.getId() + "|" + normalizar(nome);
    }

    private static String normalizar(String texto) {
        return texto.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

}
