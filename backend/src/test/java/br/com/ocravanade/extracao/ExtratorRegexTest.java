package br.com.ocravanade.extracao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ExtratorRegexTest {

	private final ExtratorRegex extrator = new ExtratorRegex();

	@Test
	void extraiTodosOsCamposDoExemploDoSlide() {
		String texto = """
				COMPROVANTE DE RESIDÊNCIA
				Nome: João da Silva
				CPF: 123.456.789-09
				Endereço: Rua das Flores, 150
				Guanambi - BA
				Data de emissão: 10/08/2026
				""";

		DadosDocumento dados = extrator.extrair(texto);

		assertThat(dados.nome()).isEqualTo("João da Silva");
		assertThat(dados.cpf()).isEqualTo("12345678909");
		assertThat(dados.endereco()).isEqualTo("Rua das Flores, 150");
		assertThat(dados.cidade()).isEqualTo("Guanambi");
		assertThat(dados.estado()).isEqualTo("BA");
		assertThat(dados.dataEmissao()).isEqualTo(LocalDate.of(2026, 8, 10));
	}

	@Test
	void toleraErrosComunsDoOcr() {
		String texto = """
				nome - Maria Souza
				cpf 98765432100
				Endereco: Av. Boa Viagem, 1000
				Emissao 01/11/2026
				""";

		DadosDocumento dados = extrator.extrair(texto);

		assertThat(dados.nome()).isEqualTo("Maria Souza");
		assertThat(dados.cpf()).isEqualTo("98765432100");
		assertThat(dados.endereco()).isEqualTo("Av. Boa Viagem, 1000");
		assertThat(dados.dataEmissao()).isEqualTo(LocalDate.of(2026, 11, 1));
	}

	@Test
	void devolveNullQuandoNaoEncontra() {
		DadosDocumento dados = extrator.extrair("texto qualquer sem campos");

		assertThat(dados).isEqualTo(new DadosDocumento(null, null, null, null, null, null));
	}

	@Test
	void dataInexistenteViraNull() {
		assertThat(extrator.extrair("Data de emissão: 31/02/2026").dataEmissao()).isNull();
	}
}
