package br.com.ocravanade.envio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.ocravanade.extracao.ExtratorRegex;
import br.com.ocravanade.imagem.ConversorDocumento;
import br.com.ocravanade.imagem.PreProcessadorImagem;
import br.com.ocravanade.ocr.LeitorOcr;
import br.com.ocravanade.validacao.ValidadorDocumento;
import java.awt.image.BufferedImage;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** OpenCV e Tesseract são nativos e lentos: aqui entram como mocks. Extrator e validador são os reais. */
class EnvioServiceTest {

	private static final String TEXTO = "Nome: Maria Souza\nData de emissão: 10/11/2026\n";
	private static final byte[] ARQUIVO = {1, 2, 3};

	private final ConversorDocumento conversor = mock(ConversorDocumento.class);
	private final PreProcessadorImagem preProcessador = mock(PreProcessadorImagem.class);
	private final LeitorOcr leitorOcr = mock(LeitorOcr.class);
	private final EnvioRepository repositorio = mock(EnvioRepository.class);
	private final Clock relogio = Clock.fixed(Instant.parse("2026-11-18T12:00:00Z"), ZoneOffset.UTC);

	private final EnvioService servico = new EnvioService(conversor, preProcessador, leitorOcr, new ExtratorRegex(),
			new ValidadorDocumento(relogio, 90), repositorio, relogio);

	@BeforeEach
	void repositorioDevolveOQueSalvou() {
		when(repositorio.save(any())).thenAnswer(chamada -> chamada.getArgument(0));
	}

	@Test
	void pdfComTextoNativoNaoPassaPeloOcr() {
		when(conversor.textoNativo(ARQUIVO, "conta.pdf")).thenReturn(Optional.of(TEXTO));

		EnvioResposta resposta = servico.processar(ARQUIVO, "conta.pdf", TipoDocumento.COMPROVANTE_RESIDENCIA);

		assertThat(resposta.textoExtraido()).isEqualTo(TEXTO);
		assertThat(resposta.dados().nome()).isEqualTo("Maria Souza");
		verifyNoInteractions(preProcessador, leitorOcr);
	}

	@Test
	void semTextoNativoUsaOpenCvEOcr() {
		BufferedImage original = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		BufferedImage tratada = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
		when(conversor.textoNativo(ARQUIVO, "foto.png")).thenReturn(Optional.empty());
		when(conversor.paraImagem(ARQUIVO, "foto.png")).thenReturn(original);
		when(preProcessador.tratar(original)).thenReturn(tratada);
		when(leitorOcr.ler(tratada)).thenReturn(TEXTO);

		EnvioResposta resposta = servico.processar(ARQUIVO, "foto.png", TipoDocumento.COMPROVANTE_RESIDENCIA);

		assertThat(resposta.textoExtraido()).isEqualTo(TEXTO);
	}
}
