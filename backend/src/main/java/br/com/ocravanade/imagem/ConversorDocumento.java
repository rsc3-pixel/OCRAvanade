package br.com.ocravanade.imagem;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/**
 * Etapa 1: o usuário manda PDF ou imagem, mas OpenCV e Tesseract trabalham com imagem.
 * O PDF é "fotografado" a 300 DPI, que é a resolução que o Tesseract lê melhor.
 */
@Component
public class ConversorDocumento {

	private static final float DPI_OCR = 300f;
	/** Abaixo disso o PDF é escaneado (no máximo um número de página ou carimbo) e precisa de OCR. */
	private static final int MINIMO_CARACTERES_TEXTO_NATIVO = 40;

	public BufferedImage paraImagem(byte[] conteudo, String nomeArquivo) {
		try {
			if (ehPdf(conteudo, nomeArquivo)) {
				try (PDDocument pdf = Loader.loadPDF(conteudo)) {
					return new PDFRenderer(pdf).renderImageWithDPI(0, DPI_OCR, ImageType.RGB);
				}
			}
			BufferedImage imagem = ImageIO.read(new ByteArrayInputStream(conteudo));
			if (imagem == null) {
				throw new DocumentoInvalidoException("Formato não suportado. Envie PDF, PNG ou JPG");
			}
			return imagem;
		} catch (IOException e) {
			throw new DocumentoInvalidoException("Não foi possível abrir o arquivo: " + e.getMessage());
		}
	}

	/**
	 * PDF gerado por sistema (conta baixada do site) já traz o texto dentro: ler direto é exato e respeita as
	 * colunas, enquanto o OCR lê a página atravessando colunas. Vazio quando é imagem ou PDF escaneado.
	 */
	public Optional<String> textoNativo(byte[] conteudo, String nomeArquivo) {
		if (!ehPdf(conteudo, nomeArquivo)) {
			return Optional.empty();
		}
		try (PDDocument pdf = Loader.loadPDF(conteudo)) {
			PDFTextStripper leitor = new PDFTextStripper();
			leitor.setStartPage(1);
			leitor.setEndPage(1);
			leitor.setLineSeparator("\n");
			String texto = leitor.getText(pdf);
			long uteis = texto.chars().filter(Character::isLetterOrDigit).count();
			return uteis >= MINIMO_CARACTERES_TEXTO_NATIVO ? Optional.of(texto) : Optional.empty();
		} catch (IOException e) {
			throw new DocumentoInvalidoException("Não foi possível abrir o arquivo: " + e.getMessage());
		}
	}

	/** Confere a assinatura "%PDF" dos primeiros bytes, não só a extensão (que o usuário pode trocar). */
	private static boolean ehPdf(byte[] conteudo, String nomeArquivo) {
		boolean assinatura = conteudo.length > 4
				&& conteudo[0] == '%' && conteudo[1] == 'P' && conteudo[2] == 'D' && conteudo[3] == 'F';
		return assinatura || (nomeArquivo != null && nomeArquivo.toLowerCase().endsWith(".pdf"));
	}
}
