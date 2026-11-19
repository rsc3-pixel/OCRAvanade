package br.com.ocravanade.imagem;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

class ConversorDocumentoTest {

	private final ConversorDocumento conversor = new ConversorDocumento();

	@Test
	void leOTextoDePdfGeradoPorSistema() throws IOException {
		byte[] pdf = pdfComLinhas("NOME DO CLIENTE:", "MARIA DAS NEVES SOUZA", "ENDERECO:", "RUA DAS PALMEIRAS 100");

		assertThat(conversor.textoNativo(pdf, "conta.pdf"))
				.hasValueSatisfying(texto -> assertThat(texto)
						.contains("NOME DO CLIENTE:\nMARIA DAS NEVES SOUZA\nENDERECO:\nRUA DAS PALMEIRAS 100"));
	}

	@Test
	void pdfEscaneadoSemTextoPrecisaDeOcr() throws IOException {
		assertThat(conversor.textoNativo(pdfComLinhas(), "escaneado.pdf")).isEmpty();
	}

	@Test
	void pdfComPoucoTextoPrecisaDeOcr() throws IOException {
		assertThat(conversor.textoNativo(pdfComLinhas("Pagina 1"), "escaneado.pdf")).isEmpty();
	}

	@Test
	void imagemPrecisaDeOcr() throws IOException {
		ByteArrayOutputStream png = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "png", png);

		assertThat(conversor.textoNativo(png.toByteArray(), "foto.png")).isEmpty();
	}

	private static byte[] pdfComLinhas(String... linhas) throws IOException {
		try (PDDocument pdf = new PDDocument()) {
			PDPage pagina = new PDPage();
			pdf.addPage(pagina);
			try (PDPageContentStream conteudo = new PDPageContentStream(pdf, pagina)) {
				conteudo.beginText();
				conteudo.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				conteudo.setLeading(16);
				conteudo.newLineAtOffset(50, 700);
				for (String linha : linhas) {
					conteudo.showText(linha);
					conteudo.newLine();
				}
				conteudo.endText();
			}
			ByteArrayOutputStream saida = new ByteArrayOutputStream();
			pdf.save(saida);
			return saida.toByteArray();
		}
	}
}
