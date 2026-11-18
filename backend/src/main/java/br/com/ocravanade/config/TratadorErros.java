package br.com.ocravanade.config;

import br.com.ocravanade.imagem.DocumentoInvalidoException;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Converte exceções em respostas HTTP padronizadas (RFC 9457 / ProblemDetail),
 * para o front sempre receber {"detail": "..."} com uma mensagem legível.
 */
@RestControllerAdvice
public class TratadorErros {

	private static final Logger log = LoggerFactory.getLogger(TratadorErros.class);

	@ExceptionHandler({ DocumentoInvalidoException.class, MethodArgumentTypeMismatchException.class })
	ProblemDetail requisicaoInvalida(Exception e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	ProblemDetail arquivoGrande() {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONTENT_TOO_LARGE, "Arquivo maior que 10 MB");
	}

	@ExceptionHandler(NoSuchElementException.class)
	ProblemDetail naoEncontrado(NoSuchElementException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	ProblemDetail falhaProcessamento(IllegalStateException e) {
		log.error("Falha ao processar documento", e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Não conseguimos processar o documento. Tente novamente");
	}
}
