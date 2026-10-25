package io.github.dedseciftm.buscaativa.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class RegraNaoEncontradaException extends ErrorResponseException {

	public RegraNaoEncontradaException(UUID id) {
		super(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Regra não encontrada: " + id),
				null);
	}

}
