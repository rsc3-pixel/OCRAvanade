package br.com.ocravanade.imagem;

/** Erro causado pelo arquivo enviado (vira HTTP 400), e não por falha do servidor. */
public class DocumentoInvalidoException extends RuntimeException {

	public DocumentoInvalidoException(String mensagem) {
		super(mensagem);
	}
}
