package io.github.dedseciftm.buscaativa.repository;

import io.github.dedseciftm.buscaativa.model.Responsavel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ResponsavelRepository extends JpaRepository<Responsavel, UUID> {

    List<Responsavel> findByEmailIn(Collection<String> emails);

    List<Responsavel> findByTelefoneIn(Collection<String> telefones);

}
