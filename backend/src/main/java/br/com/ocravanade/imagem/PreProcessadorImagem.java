package br.com.ocravanade.imagem;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.imageio.ImageIO;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

/**
 * Etapa 2 (OpenCV): deixa a imagem "fácil de ler" antes do OCR.
 * Analogia: é passar o documento a limpo antes de entregar para alguém digitar.
 */
@Component
public class PreProcessadorImagem {

	/** Abaixo disso as letras ficam pequenas demais para o Tesseract. */
	private static final int LARGURA_MINIMA = 1600;
	/** Inclinação maior que isso provavelmente é erro de detecção, não foto torta. */
	private static final double ANGULO_MAXIMO = 15.0;

	static {
		OpenCV.loadLocally();
	}

	public BufferedImage tratar(BufferedImage original) {
		Mat colorida = paraMat(original);
		Mat cinza = new Mat();
		Imgproc.cvtColor(colorida, cinza, Imgproc.COLOR_BGR2GRAY);

		Mat ampliada = ampliarSePequena(cinza);

		Mat semRuido = new Mat();
		Imgproc.medianBlur(ampliada, semRuido, 3);

		// Limiar adaptativo: decide preto/branco por vizinhança, então sombra num canto não apaga o texto.
		Mat pretoBranco = new Mat();
		Imgproc.adaptiveThreshold(semRuido, pretoBranco, 255,
				Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 31, 15);

		return paraBufferedImage(corrigirRotacao(pretoBranco));
	}

	private static Mat ampliarSePequena(Mat imagem) {
		if (imagem.cols() >= LARGURA_MINIMA) {
			return imagem;
		}
		double escala = (double) LARGURA_MINIMA / imagem.cols();
		Mat ampliada = new Mat();
		Imgproc.resize(imagem, ampliada, new Size(), escala, escala, Imgproc.INTER_CUBIC);
		return ampliada;
	}

	/**
	 * Acha o menor retângulo que envolve todos os pixels de texto e lê o ângulo dele.
	 * Se o retângulo está inclinado, a foto está torta: giramos de volta.
	 */
	private static Mat corrigirRotacao(Mat pretoBranco) {
		Mat invertida = new Mat();
		Core.bitwise_not(pretoBranco, invertida);
		MatOfPoint pontosTexto = new MatOfPoint();
		Core.findNonZero(invertida, pontosTexto);
		if (pontosTexto.empty()) {
			return pretoBranco;
		}

		double angulo = Imgproc.minAreaRect(new MatOfPoint2f(pontosTexto.toArray())).angle;
		if (angulo > 45) {
			angulo -= 90;
		}
		if (Math.abs(angulo) < 0.3 || Math.abs(angulo) > ANGULO_MAXIMO) {
			return pretoBranco;
		}

		Point centro = new Point(pretoBranco.cols() / 2.0, pretoBranco.rows() / 2.0);
		Mat rotacao = Imgproc.getRotationMatrix2D(centro, angulo, 1.0);
		Mat endireitada = new Mat();
		Imgproc.warpAffine(pretoBranco, endireitada, rotacao, pretoBranco.size(),
				Imgproc.INTER_CUBIC, Core.BORDER_REPLICATE);
		return endireitada;
	}

	private static Mat paraMat(BufferedImage imagem) {
		try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
			ImageIO.write(imagem, "png", saida);
			return Imgcodecs.imdecode(new MatOfByte(saida.toByteArray()), Imgcodecs.IMREAD_COLOR);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static BufferedImage paraBufferedImage(Mat mat) {
		MatOfByte png = new MatOfByte();
		Imgcodecs.imencode(".png", mat, png);
		try {
			return ImageIO.read(new ByteArrayInputStream(png.toArray()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
