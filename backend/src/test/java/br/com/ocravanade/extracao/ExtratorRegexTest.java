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

	/** Layout real de uma conta de luz (texto nativo do PDF), com dados pessoais trocados por fictícios. */
	private static final String CONTA_DE_LUZ = """
			 CLASSIFICAÇÃO: B1 RESIDENCIAL -RESIDENCIAL  TIPO DE FORNECIMENTO: Conv. Monômia - Monofásico
			 NOME DO CLIENTE:
			 MARIA DAS NEVES SOUZA
			 CPF: 123.4**.***-**   \s
			 ENDEREÇO:
			 RUA DAS PALMEIRAS 100  AP-201
			 EDIFICIO SOL NASCENTE
			 CANDEIAS
			 54440-000 JABOATAO DOS GUARARAPES PE
			NÚMERO DA UNIDADE CONSUMIDORA
			3.111.222.333-44
			 NOTA FISCAL Nº 100200300 - SÉRIE 000 / DATA DE EMISSÃO: 28/10/2026
			""";

	@Test
	void contaDeLuzComValorNaLinhaDeBaixoDoRotulo() {
		DadosDocumento dados = extrator.extrair(CONTA_DE_LUZ);

		assertThat(dados.nome()).isEqualTo("MARIA DAS NEVES SOUZA");
		assertThat(dados.endereco()).isEqualTo("RUA DAS PALMEIRAS 100 AP-201");
		assertThat(dados.dataEmissao()).isEqualTo(LocalDate.of(2026, 10, 28));
	}

	@Test
	void contaDeLuzComCidadeEUfDepoisDoCep() {
		DadosDocumento dados = extrator.extrair(CONTA_DE_LUZ);

		assertThat(dados.cidade()).isEqualTo("JABOATAO DOS GUARARAPES");
		assertThat(dados.estado()).isEqualTo("PE");
	}

	@Test
	void naoConfundeOutroNumeroComCpfQuandoOCpfEstaMascarado() {
		assertThat(extrator.extrair(CONTA_DE_LUZ).cpf()).isNull();
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
