package io.github.dedseciftm.buscaativa.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Responde os erros no formato Problem Details (RFC 9457). Em erros de validação, inclui
 * {@code erros} com a mensagem de cada campo inválido.
 */
@RestControllerAdvice
public class TratamentoDeErros extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> erros = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach((erro) -> erros.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

		var problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Alguns campos estão inválidos.");
		problema.setProperty("erros", erros);
		return handleExceptionInternal(ex, problema, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		var problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Os dados enviados não puderam ser lidos. Verifique se todos os campos estão corretos.");
		return handleExceptionInternal(ex, problema, headers, status, request);
	}

}
