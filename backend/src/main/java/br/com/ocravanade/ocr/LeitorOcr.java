package br.com.ocravanade.ocr;

import br.com.ocravanade.config.OcrProperties;
import java.awt.image.BufferedImage;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;

/**
 * Etapa 3 (Tesseract): imagem entra, texto sai. Ele reconhece letras, mas não sabe o que é um CPF.
 */
@Component
public class LeitorOcr {

	/** "Página com blocos de texto": o modo que funciona melhor para contas e comprovantes. */
	private static final int MODO_PAGINA_AUTOMATICO = 3;

	private final OcrProperties props;

	public LeitorOcr(OcrProperties props) {
		this.props = props;
	}

	public String ler(BufferedImage imagem) {
		// Uma instância por chamada: o Tesseract não é thread-safe e duas requisições ao mesmo tempo se atropelariam.
		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath(props.tessdata());
		tesseract.setLanguage(props.idioma());
		tesseract.setPageSegMode(MODO_PAGINA_AUTOMATICO);
		try {
			return tesseract.doOCR(imagem);
		} catch (TesseractException e) {
			throw new IllegalStateException("Falha no OCR: " + e.getMessage(), e);
		}
	}
}
