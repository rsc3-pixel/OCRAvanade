package br.com.ocravanade.envio;

import br.com.ocravanade.extracao.DadosDocumento;
import br.com.ocravanade.extracao.ExtratorDados;
import br.com.ocravanade.imagem.ConversorDocumento;
import br.com.ocravanade.imagem.PreProcessadorImagem;
import br.com.ocravanade.ocr.LeitorOcr;
import br.com.ocravanade.validacao.ResultadoValidacao;
import br.com.ocravanade.validacao.ValidadorDocumento;
import java.awt.image.BufferedImage;
import java.time.Clock;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * O maestro: chama cada etapa na ordem do slide.
 * preparar (OpenCV) -> ler (Tesseract) -> interpretar (Regex/IA) -> validar -> persistir.
 */
@Service
public class EnvioService {

	private static final Logger log = LoggerFactory.getLogger(EnvioService.class);

	private final ConversorDocumento conversor;
	private final PreProcessadorImagem preProcessador;
	private final LeitorOcr leitorOcr;
	private final ExtratorDados extrator;
	private final ValidadorDocumento validador;
	private final EnvioRepository repositorio;
	private final Clock relogio;

	public EnvioService(ConversorDocumento conversor, PreProcessadorImagem preProcessador, LeitorOcr leitorOcr,
			ExtratorDados extrator, ValidadorDocumento validador, EnvioRepository repositorio, Clock relogio) {
		this.conversor = conversor;
		this.preProcessador = preProcessador;
		this.leitorOcr = leitorOcr;
		this.extrator = extrator;
		this.validador = validador;
		this.repositorio = repositorio;
		this.relogio = relogio;
	}

	@Transactional
	public EnvioResposta processar(byte[] conteudo, String nomeArquivo, TipoDocumento tipo) {
		long inicio = System.currentTimeMillis();

		BufferedImage original = conversor.paraImagem(conteudo, nomeArquivo);
		BufferedImage tratada = preProcessador.tratar(original);
		String texto = leitorOcr.ler(tratada);
		DadosDocumento dados = extrator.extrair(texto);
		ResultadoValidacao resultado = validador.validar(texto, dados);

		Envio envio = repositorio.save(
				Envio.processado(nomeArquivo, tipo, extrator.nome(), texto, dados, resultado, relogio.instant()));
		log.info("Envio {} processado em {} ms: {}", envio.getId(), System.currentTimeMillis() - inicio, envio.getStatus());
		return EnvioResposta.de(envio);
	}

	@Transactional(readOnly = true)
	public List<EnvioResposta> listar() {
		return repositorio.findTop50ByOrderByIdDesc().stream().map(EnvioResposta::de).toList();
	}

	@Transactional(readOnly = true)
	public EnvioResposta buscar(Long id) {
		return repositorio.findById(id).map(EnvioResposta::de)
				.orElseThrow(() -> new NoSuchElementException("Envio " + id + " não encontrado"));
	}
}
