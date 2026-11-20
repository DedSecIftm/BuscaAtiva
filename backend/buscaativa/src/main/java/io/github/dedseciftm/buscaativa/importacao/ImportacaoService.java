package io.github.dedseciftm.buscaativa.importacao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.dedseciftm.buscaativa.model.Aluno;
import io.github.dedseciftm.buscaativa.model.Turma;
import io.github.dedseciftm.buscaativa.repository.AlunoRepository;
import io.github.dedseciftm.buscaativa.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria ou atualiza turmas e alunos a partir da planilha. Reimportar o mesmo arquivo não duplica nada, e um aluno
 * repetido no mesmo arquivo vale pela última linha.
 */
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

		// Uma consulta para todas as turmas e outra para todos os alunos do arquivo, em vez de uma por linha.
		var idsDasTurmas = lido.linhas().stream().map(l -> l.turma().idTurma()).collect(Collectors.toSet());
		var idsDosAlunos = lido.linhas().stream().map(LinhaDaPlanilha::idDoAluno).collect(Collectors.toSet());
		Map<String, Turma> turmasPorId = new HashMap<>();
		turmas.findAllById(idsDasTurmas).forEach(t -> turmasPorId.put(t.getIdTurma(), t));
		Map<String, Aluno> alunosPorId = new HashMap<>();
		alunos.findAllById(idsDosAlunos).forEach(a -> alunosPorId.put(a.getIdDoAluno(), a));

		var turmasNovas = new ArrayList<Turma>();
		var alunosNovos = new ArrayList<Aluno>();
		var idsNovos = new HashSet<String>();
		var atualizados = new HashSet<String>();

		for (var linha : lido.linhas()) {
			var dados = linha.turma();
			Turma turma = turmasPorId.get(dados.idTurma());
			if (turma == null) {
				turma = new Turma(dados.idTurma(), dados.ano(), dados.nivelEscolar(), dados.tipoEnsino(), null);
				turmasPorId.put(dados.idTurma(), turma);
				turmasNovas.add(turma);
			}
			else {
				turma.setAno(dados.ano());
				turma.setNivelEscolar(dados.nivelEscolar());
				turma.setTipoEnsino(dados.tipoEnsino());
			}

			Aluno aluno = alunosPorId.get(linha.idDoAluno());
			if (aluno == null) {
				aluno = new Aluno(linha.idDoAluno(), turma, linha.nome());
				alunosPorId.put(linha.idDoAluno(), aluno);
				alunosNovos.add(aluno);
				idsNovos.add(linha.idDoAluno());
			}
			else {
				aluno.setNome(linha.nome());
				aluno.setTurma(turma);
				if (!idsNovos.contains(aluno.getIdDoAluno())) {
					atualizados.add(aluno.getIdDoAluno());
				}
			}
		}

		turmas.saveAll(turmasNovas);
		alunos.saveAll(alunosNovos);
		return new ResultadoDaImportacao(turmasNovas.size(), alunosNovos.size(), atualizados.size(),
				lido.erros().size(), List.copyOf(lido.erros()));
	}

}
