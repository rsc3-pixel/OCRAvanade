package br.com.ocravanade.config;

import br.com.ocravanade.extracao.ExtratorClaude;
import br.com.ocravanade.extracao.ExtratorDados;
import br.com.ocravanade.extracao.ExtratorRegex;
import br.com.ocravanade.validacao.ValidadorDocumento;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Monta as peças que não são @Component porque dependem de configuração.
 * Trocar Regex por IA é mudar uma variável de ambiente (OCR_EXTRATOR), sem tocar no código.
 */
@Configuration
public class PipelineConfig {

	@Bean
	Clock relogio() {
		return Clock.system(ZoneId.of("America/Recife"));
	}

	@Bean
	ValidadorDocumento validadorDocumento(Clock relogio, OcrProperties props) {
		return new ValidadorDocumento(relogio, props.validadeDias());
	}

	@Bean
	ExtratorDados extratorDados(OcrProperties props) {
		ExtratorRegex regex = new ExtratorRegex();
		if (props.extrator() == OcrProperties.Extrator.CLAUDE) {
			return new ExtratorClaude(AnthropicOkHttpClient.fromEnv(), props.modeloIa(), regex);
		}
		return regex;
	}
}
