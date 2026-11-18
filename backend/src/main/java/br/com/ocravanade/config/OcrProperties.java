package br.com.ocravanade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tudo que muda entre ambientes vem do application.properties (que lê variáveis de ambiente).
 *
 * @param tessdata     pasta com os modelos treinados do Tesseract (por.traineddata)
 * @param idioma       idioma do OCR ("por")
 * @param extrator     REGEX (padrão, sem custo) ou CLAUDE (LLM)
 * @param modeloIa     id do modelo usado quando extrator = CLAUDE
 * @param validadeDias idade máxima do comprovante
 */
@ConfigurationProperties(prefix = "ocr")
public record OcrProperties(
		String tessdata,
		String idioma,
		Extrator extrator,
		String modeloIa,
		int validadeDias) {

	public enum Extrator {
		REGEX, CLAUDE
	}
}
