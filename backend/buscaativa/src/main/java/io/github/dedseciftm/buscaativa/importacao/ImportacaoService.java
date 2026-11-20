package io.github.dedseciftm.buscaativa.importacao;

import java.io.IOException;
import java.io.InputStream;

import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.model.Turma;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cria ou atualiza turmas e alunos a partir da planilha. Reimportar o mesmo arquivo não duplica nada. */
@Service
public class ImportacaoService {

	private final LeitorDePlanilha leitor;
	private final TurmaRepository turmas;
	private final AlunoRepository alunos;

	public ImportacaoService(LeitorDePlanilha leitor, TurmaRepository turmas, AlunoRepository alunos) {
		this.leitor = leitor;
		this.turmas = turmas;
		this.alunos = alunos;
	}

	@Transactional
	public ResultadoDaImportacao importar(InputStream arquivo) throws IOException {
		var lido = leitor.ler(arquivo);
		int turmasCriadas = 0, alunosCriados = 0, alunosAtualizados = 0;

		for (var linha : lido.linhas()) {
			var dados = linha.turma();
			Turma turma = turmas.findById(dados.idTurma()).orElse(null);

			if (turma == null) {
				turma = turmas.save(new Turma(dados.idTurma(), dados.ano(), dados.nivelEscolar(), dados.tipoEnsino(), null));
				turmasCriadas++;
			}
			else {
				turma.setAno(dados.ano());
				turma.setNivelEscolar(dados.nivelEscolar());
				turma.setTipoEnsino(dados.tipoEnsino());
			}

			Aluno aluno = alunos.findById(linha.idDoAluno()).orElse(null);
			if (aluno == null) {
				alunos.save(new Aluno(linha.idDoAluno(), turma, linha.nome()));
				alunosCriados++;
			}
			else {
				aluno.setNome(linha.nome());
				aluno.setTurma(turma);
				alunosAtualizados++;
			}
		}
		return new ResultadoDaImportacao(turmasCriadas, alunosCriados, alunosAtualizados, lido.erros().size(),
				lido.erros());
	}

}
