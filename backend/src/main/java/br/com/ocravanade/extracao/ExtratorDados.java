package br.com.ocravanade.extracao;

/**
 * Etapa 4: transforma o texto cru do Tesseract em campos estruturados.
 * Duas implementações: {@link ExtratorRegex} (sem custo) e {@link ExtratorClaude} (LLM).
 */
public interface ExtratorDados {

	DadosDocumento extrair(String textoOcr);

	String nome();
}
