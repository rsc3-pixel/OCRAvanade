package br.com.ocravanade.envio;

import br.com.ocravanade.validacao.RegraAplicada;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Versão "de banco" de {@link RegraAplicada}: cada envio guarda o resultado de cada regra. */
@Embeddable
public class RegraVerificada {

	private String regra;
	private boolean aprovada;
	@Column(length = 500)
	private String detalhe;

	protected RegraVerificada() {
	}

	RegraVerificada(RegraAplicada origem) {
		this.regra = origem.regra();
		this.aprovada = origem.aprovada();
		this.detalhe = origem.detalhe();
	}

	RegraAplicada paraRegraAplicada() {
		return new RegraAplicada(regra, aprovada, detalhe);
	}
}
