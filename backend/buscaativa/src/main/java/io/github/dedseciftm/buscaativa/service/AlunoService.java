package io.github.dedseciftm.buscaativa.service;

import io.github.dedseciftm.buscaativa.dto.AlunoRequest;
import io.github.dedseciftm.buscaativa.dto.AlunoResponse;
import io.github.dedseciftm.buscaativa.exception.AlunoNaoEncontradoException;
import io.github.dedseciftm.buscaativa.exception.MatriculaEmUsoException;
import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.model.Turma;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AlunoService {

    private final AlunoRepository repository;
    private final TurmaRepository turmaRepository;

    public AlunoService(AlunoRepository alunoRepository, TurmaRepository turmaRepository) {
        this.repository = alunoRepository;
        this.turmaRepository = turmaRepository;
    }

    public List<AlunoResponse> listar() {
        return repository.findByExcluidoEmIsNullOrderByNomeAsc().stream().map(AlunoResponse::de).toList();
    }

    public AlunoResponse buscar(UUID id) {
        return AlunoResponse.de(encontrar(id));
    }

    /** Cadastrar de novo a matrícula de um aluno excluído traz esse aluno de volta, com os novos dados. */
    @Transactional
    public AlunoResponse criar(AlunoRequest dados) {
        var matricula = dados.matricula().trim();
        var aluno = repository.findByMatricula(matricula).orElseGet(Aluno::new);
        if (aluno.getId() != null && aluno.getExcluidoEm() == null) {
            throw new MatriculaEmUsoException(matricula);
        }
        aluno.setExcluidoEm(null);
        preencher(aluno, dados);
        return AlunoResponse.de(repository.saveAndFlush(aluno));
    }

    @Transactional
    public AlunoResponse atualizar(UUID id, AlunoRequest dados) {
        var aluno = encontrar(id);
        var matricula = dados.matricula().trim();
        if (repository.existsByMatriculaAndIdNot(matricula, id)) {
            throw new MatriculaEmUsoException(matricula);
        }
        preencher(aluno, dados);
        return AlunoResponse.de(aluno);
    }

    @Transactional
    public void excluir(UUID id) {
        encontrar(id).setExcluidoEm(Instant.now());
    }

    private Aluno encontrar(UUID id) {
        return repository.findByIdAndExcluidoEmIsNull(id).orElseThrow(() -> new AlunoNaoEncontradoException(id));
    }

    private void preencher(Aluno aluno, AlunoRequest dados) {
        aluno.setNome(dados.nome().trim());
        aluno.setMatricula(dados.matricula().trim());
        aluno.setEmail(textoOuNulo(dados.email()));
        aluno.setTelefone(textoOuNulo(dados.telefone()));
        aluno.setTurma(turmaDe(dados.turma().trim()));
        if (dados.situacao() != null) {
            aluno.setSituacao(dados.situacao());
        }
    }

    private Turma turmaDe(String nome) {
        return turmaRepository.findByNomeIgnoreCase(nome).orElseGet(() -> turmaRepository.save(new Turma(nome)));
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

}
