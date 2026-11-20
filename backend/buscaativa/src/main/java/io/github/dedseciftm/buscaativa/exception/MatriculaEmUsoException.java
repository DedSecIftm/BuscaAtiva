package io.github.dedseciftm.buscaativa.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class MatriculaEmUsoException extends ErrorResponseException {

    public MatriculaEmUsoException(String matricula) {
        super(HttpStatus.CONFLICT, problema(matricula), null);
    }

    private static ProblemDetail problema(String matricula) {
        var mensagem = "Já existe um aluno com a matrícula " + matricula + ".";
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, mensagem);
        problema.setProperty("erros", Map.of("matricula", mensagem));
        return problema;
    }

}
