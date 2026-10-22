package io.github.dedseciftm.buscaativa.regra;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RegraService {

	private final RegraRepository repository;

	public RegraService(RegraRepository repository) {
		this.repository = repository;
	}

	public List<RegraResponse> listar() {
		return repository.findAllByOrderByCriadoEmAsc().stream().map(RegraResponse::de).toList();
	}

	public RegraResponse buscar(UUID id) {
		return RegraResponse.de(encontrar(id));
	}

	@Transactional
	public RegraResponse criar(RegraRequest dados) {
		var regra = new RegraIrregularidade(dados.nome().trim(), dados.tipo(), dados.quantidadeFaltas(),
				periodoDe(dados));
		if (dados.ativa() != null) {
			regra.setAtiva(dados.ativa());
		}
		return RegraResponse.de(repository.saveAndFlush(regra));
	}

	@Transactional
	public RegraResponse atualizar(UUID id, RegraRequest dados) {
		var regra = encontrar(id);
		regra.setNome(dados.nome().trim());
		regra.setTipo(dados.tipo());
		regra.setQuantidadeFaltas(dados.quantidadeFaltas());
		regra.setPeriodoEmDias(periodoDe(dados));
		if (dados.ativa() != null) {
			regra.setAtiva(dados.ativa());
		}
		return RegraResponse.de(regra);
	}

	@Transactional
	public void excluir(UUID id) {
		repository.delete(encontrar(id));
	}

	private RegraIrregularidade encontrar(UUID id) {
		return repository.findById(id).orElseThrow(() -> new RegraNaoEncontradaException(id));
	}

	// O período só faz sentido em regras NO_PERIODO; nas demais é descartado.
	private static Integer periodoDe(RegraRequest dados) {
		return dados.tipo() == TipoRegra.NO_PERIODO ? dados.periodoEmDias() : null;
	}

}
