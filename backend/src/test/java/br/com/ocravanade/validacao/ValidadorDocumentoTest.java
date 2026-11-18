package br.com.ocravanade.validacao;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ocravanade.extracao.DadosDocumento;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ValidadorDocumentoTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 11, 18);
	private static final Clock RELOGIO = Clock.fixed(HOJE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
	private static final String TEXTO_LEGIVEL = "COMPROVANTE DE RESIDENCIA Nome Joao da Silva Rua das Flores 150 Guanambi BA";

	private final ValidadorDocumento validador = new ValidadorDocumento(RELOGIO, 90);

	private static DadosDocumento dadosValidos(LocalDate emissao) {
		return new DadosDocumento("João da Silva", "12345678909", "Rua das Flores, 150", "Guanambi", "BA", emissao);
	}

	@Test
	void aprovaQuandoTodasAsRegrasPassam() {
		ResultadoValidacao resultado = validador.validar(TEXTO_LEGIVEL, dadosValidos(HOJE.minusDays(10)));

		assertThat(resultado.aprovado()).isTrue();
		assertThat(resultado.regras()).hasSize(5).allMatch(RegraAplicada::aprovada);
	}

	@Test
	void reprovaDocumentoComMaisDe90Dias() {
		ResultadoValidacao resultado = validador.validar(TEXTO_LEGIVEL, dadosValidos(HOJE.minusDays(91)));

		assertThat(resultado.aprovado()).isFalse();
		assertThat(resultado.regras()).filteredOn(r -> !r.aprovada())
				.singleElement()
				.extracting(RegraAplicada::detalhe).asString().contains("91 dias");
	}

	@Test
	void aceitaExatamente90Dias() {
		assertThat(validador.validar(TEXTO_LEGIVEL, dadosValidos(HOJE.minusDays(90))).aprovado()).isTrue();
	}

	@Test
	void reprovaDataNoFuturo() {
		assertThat(validador.validar(TEXTO_LEGIVEL, dadosValidos(HOJE.plusDays(1))).aprovado()).isFalse();
	}

	@Test
	void reprovaTextoIlegivel() {
		ResultadoValidacao resultado = validador.validar("@@ ## !!", dadosValidos(HOJE));

		assertThat(resultado.aprovado()).isFalse();
		assertThat(resultado.regras().getFirst().aprovada()).isFalse();
	}

	@Test
	void mascaraOCpfNoDetalhe() {
		ResultadoValidacao resultado = validador.validar(TEXTO_LEGIVEL, dadosValidos(HOJE));

		assertThat(resultado.regras()).extracting(RegraAplicada::detalhe).contains("123.***.***-09");
	}

	@Test
	void reprovaCamposAusentes() {
		ResultadoValidacao resultado = validador.validar(TEXTO_LEGIVEL, new DadosDocumento(null, "123", " ", null, null, HOJE));

		assertThat(resultado.regras()).filteredOn(r -> !r.aprovada())
				.extracting(RegraAplicada::regra)
				.containsExactly("Nome encontrado", "CPF válido", "Endereço encontrado");
	}

	@Test
	void reprovaCpfComDigitoVerificadorErrado() {
		DadosDocumento dados = new DadosDocumento("João da Silva", "12345678900", "Rua das Flores, 150", "Guanambi", "BA", HOJE);

		ResultadoValidacao resultado = validador.validar(TEXTO_LEGIVEL, dados);

		assertThat(resultado.aprovado()).isFalse();
		assertThat(resultado.regras()).filteredOn(r -> !r.aprovada())
				.singleElement()
				.satisfies(r -> {
					assertThat(r.regra()).isEqualTo("CPF válido");
					assertThat(r.detalhe()).contains("dígito verificador");
				});
	}

	@Test
	void reprovaCpfComTodosOsDigitosIguais() {
		DadosDocumento dados = new DadosDocumento("João da Silva", "11111111111", "Rua das Flores, 150", "Guanambi", "BA", HOJE);

		assertThat(validador.validar(TEXTO_LEGIVEL, dados).aprovado()).isFalse();
	}
}
