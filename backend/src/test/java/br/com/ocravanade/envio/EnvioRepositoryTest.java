package br.com.ocravanade.envio;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ocravanade.extracao.DadosDocumento;
import br.com.ocravanade.validacao.RegraAplicada;
import br.com.ocravanade.validacao.ResultadoValidacao;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/** Sobe só a camada JPA num banco H2 em memória: confere se a entidade e as regras são gravadas e lidas certo. */
@DataJpaTest
class EnvioRepositoryTest {

	@Autowired
	private EnvioRepository repositorio;

	@Test
	void salvaEnvioComRegrasNaOrdemEMascaraCpfNaResposta() {
		DadosDocumento dados = new DadosDocumento("João da Silva", "12345678909", "Rua das Flores, 150",
				"Guanambi", "BA", LocalDate.of(2026, 11, 10));
		ResultadoValidacao resultado = new ResultadoValidacao(false, List.of(
				new RegraAplicada("Documento legível", true, "120 caracteres lidos"),
				new RegraAplicada("Emissão nos últimos 90 dias", false, "Documento antigo")));

		Envio salvo = repositorio.saveAndFlush(Envio.processado("conta.pdf", TipoDocumento.COMPROVANTE_RESIDENCIA,
				"regex", "texto", dados, resultado, Instant.parse("2026-11-18T12:00:00Z")));

		EnvioResposta resposta = EnvioResposta.de(repositorio.findById(salvo.getId()).orElseThrow());

		assertThat(resposta.status()).isEqualTo(StatusEnvio.REPROVADO);
		assertThat(resposta.dados().cpf()).isEqualTo("123.***.***-09");
		assertThat(resposta.regras()).extracting(RegraAplicada::regra)
				.containsExactly("Documento legível", "Emissão nos últimos 90 dias");
		assertThat(repositorio.findTop50ByOrderByIdDesc()).hasSize(1);
	}
}
