package io.github.dedseciftm.buscaativa.service;

import io.github.dedseciftm.buscaativa.dto.ImportacaoAlunosRequest.Linha;
import io.github.dedseciftm.buscaativa.model.Parentesco;
import io.github.dedseciftm.buscaativa.model.Responsavel;
import io.github.dedseciftm.buscaativa.repository.ResponsavelRepository;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Localiza ou cria os responsáveis de um arquivo: primeiro pelo e-mail e depois pelo telefone, tanto na base
 * quanto nas linhas já processadas. Quem já existe só tem os campos vazios completados: como pais podem
 * dividir um telefone, sobrescrever trocaria os dados de um pelos do outro.
 */
class   ResponsaveisDoLote {

    private final ResponsavelRepository repository;
    private final Map<String, Responsavel> porEmail = new HashMap<>();
    private final Map<String, Responsavel> porTelefone = new HashMap<>();
    private int criados;

    ResponsaveisDoLote(ResponsavelRepository repository, List<Linha> linhas) {
        this.repository = repository;
        var comResponsavel = linhas.stream().filter(Linha::temResponsavel).toList();
        // Buscados de uma vez para não fazer uma consulta por registro em arquivos grandes.
        repository.findByEmailIn(comResponsavel.stream().map(ResponsaveisDoLote::emailDe)
                        .filter(Objects::nonNull).distinct().toList())
                .forEach(this::indexar);
        repository.findByTelefoneIn(comResponsavel.stream().map(ResponsaveisDoLote::telefoneDe)
                        .filter(Objects::nonNull).distinct().toList())
                .forEach(this::indexar);
    }

    Responsavel localizarOuCriar(Linha linha) {
        var email = emailDe(linha);
        var telefone = telefoneDe(linha);
        var responsavel = email == null ? null : porEmail.get(email);
        if (responsavel == null && telefone != null) {
            responsavel = porTelefone.get(telefone);
        }
        if (responsavel == null) {
            responsavel = new Responsavel();
            responsavel.setNome(linha.responsavelNome().trim());
            responsavel.setEmail(email);
            responsavel.setTelefone(telefone);
            repository.save(responsavel);
            criados++;
        } else {
            if (responsavel.getEmail() == null) {
                responsavel.setEmail(email);
            }
            if (responsavel.getTelefone() == null) {
                responsavel.setTelefone(telefone);
            }
        }
        indexar(responsavel);
        return responsavel;
    }

    int criados() {
        return criados;
    }

    private void indexar(Responsavel responsavel) {
        if (responsavel.getEmail() != null) {
            porEmail.putIfAbsent(responsavel.getEmail(), responsavel);
        }
        if (responsavel.getTelefone() != null) {
            porTelefone.putIfAbsent(responsavel.getTelefone(), responsavel);
        }
    }

    private static String emailDe(Linha linha) {
        var email = linha.responsavelEmail();
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String telefoneDe(Linha linha) {
        var digitos = linha.responsavelTelefone() == null ? "" : linha.responsavelTelefone().replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }

    /**
     * Devolve {@code null} quando o parentesco não foi informado. Fora da lista, como "avó", vira OUTRO:
     * isso não é motivo para perder o registro do aluno.
     */
    static Parentesco parentescoDe(Linha linha) {
        var texto = linha.responsavelParentesco();
        if (texto == null || texto.isBlank()) {
            return null;
        }
        var semAcento = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return switch (semAcento.toLowerCase(Locale.ROOT)) {
            case "pai" -> Parentesco.PAI;
            case "mae" -> Parentesco.MAE;
            case "tutor", "tutora" -> Parentesco.TUTOR;
            case "tio", "tia" -> Parentesco.TIO;
            default -> Parentesco.OUTRO;
        };
    }

}
