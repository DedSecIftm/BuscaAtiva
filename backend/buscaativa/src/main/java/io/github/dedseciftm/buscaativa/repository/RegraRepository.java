package io.github.dedseciftm.buscaativa.repository;

import java.util.List;
import java.util.UUID;

import io.github.dedseciftm.buscaativa.model.RegraIrregularidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegraRepository extends JpaRepository<RegraIrregularidade, UUID> {

	List<RegraIrregularidade> findAllByOrderByCriadoEmAsc();

}
