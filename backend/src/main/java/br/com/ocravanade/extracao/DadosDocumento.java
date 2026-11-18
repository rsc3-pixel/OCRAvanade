package br.com.ocravanade.extracao;

import java.time.LocalDate;

/**
 * Etapa 5 do slide: o JSON vira um objeto Java. Campo não encontrado fica null.
 */
public record DadosDocumento(
		String nome,
		String cpf,
		String endereco,
		String cidade,
		String estado,
		LocalDate dataEmissao) {
}
