package io.github.dedseciftm.buscaativa.importacao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class PlanilhaInvalidaException extends ErrorResponseException {

	public PlanilhaInvalidaException(String detalhe) {
		super(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalhe), null);
	}

}
