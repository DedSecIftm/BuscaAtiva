package io.github.dedseciftm.buscaativa.repository;

import io.github.dedseciftm.buscaativa.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TurmaRepository extends JpaRepository<Turma, UUID> {

    Optional<Turma> findByNomeIgnoreCase(String nome);

}
