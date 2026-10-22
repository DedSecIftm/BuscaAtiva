package io.github.dedseciftm.buscaativa.regra;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegraRepository extends JpaRepository<RegraIrregularidade, UUID> {

	List<RegraIrregularidade> findAllByOrderByCriadoEmAsc();

}
