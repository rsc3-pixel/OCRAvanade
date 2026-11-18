package br.com.ocravanade.validacao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

	@ParameterizedTest
	@ValueSource(strings = {"12345678909", "52998224725"})
	void aceitaCpfComDigitosVerificadoresCorretos(String cpf) {
		assertThat(Cpf.valido(cpf)).isTrue();
	}

	@Test
	void rejeitaPrimeiroDigitoVerificadorErrado() {
		assertThat(Cpf.valido("12345678919")).isFalse();
	}

	@Test
	void rejeitaSegundoDigitoVerificadorErrado() {
		assertThat(Cpf.valido("12345678900")).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"00000000000", "11111111111", "99999999999"})
	void rejeitaTodosOsDigitosIguais(String cpf) {
		assertThat(Cpf.valido(cpf)).isFalse();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"123", "123456789090", "123.456.789-09", "1234567890a"})
	void rejeitaFormatoQueNaoSaoOnzeDigitos(String cpf) {
		assertThat(Cpf.valido(cpf)).isFalse();
	}

	@Test
	void mascaraOsDigitosDoMeio() {
		assertThat(Cpf.mascarar("12345678909")).isEqualTo("123.***.***-09");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"123"})
	void naoMascaraQuandoNaoTemOnzeDigitos(String cpf) {
		assertThat(Cpf.mascarar(cpf)).isEqualTo(cpf);
	}
}
