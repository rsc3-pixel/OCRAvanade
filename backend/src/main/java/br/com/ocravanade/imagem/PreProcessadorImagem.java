package br.com.ocravanade.imagem;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
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

		// Filtra o ruído ANTES de ampliar: ampliar primeiro aumentaria os pontinhos junto com as letras.
		Mat semRuido = new Mat();
		Imgproc.medianBlur(cinza, semRuido, 3);
		Mat ampliada = ampliarSePequena(semRuido);

		// Limiar adaptativo: decide preto/branco por vizinhança, então sombra num canto não apaga o texto.
		Mat pretoBranco = new Mat();
		Imgproc.adaptiveThreshold(ampliada, pretoBranco, 255,
				Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 31, 15);
		// Segunda limpeza: some com pontos isolados que sobreviveram ao limiar.
		Imgproc.medianBlur(pretoBranco, pretoBranco, 3);

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
	 * Mede a inclinação pelas linhas de texto:
	 * 1. borra na horizontal para cada linha de texto virar um "bloco" comprido;
	 * 2. ignora blocos curtos (ruído, manchas);
	 * 3. usa a mediana dos ângulos, que não se deixa levar por um bloco esquisito.
	 * Se as linhas estão inclinadas, a foto está torta: giramos de volta.
	 */
	private static Mat corrigirRotacao(Mat pretoBranco) {
		double angulo = inclinacaoDasLinhas(pretoBranco);
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

	private static double inclinacaoDasLinhas(Mat pretoBranco) {
		Mat texto = new Mat();
		Core.bitwise_not(pretoBranco, texto);
		Size pincel = new Size(Math.max(15, pretoBranco.cols() / 40), 3);
		Imgproc.dilate(texto, texto, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, pincel));

		List<MatOfPoint> blocos = new ArrayList<>();
		Imgproc.findContours(texto, blocos, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		double larguraMinima = pretoBranco.cols() * 0.15;
		List<Double> angulos = new ArrayList<>();
		for (MatOfPoint bloco : blocos) {
			RotatedRect r = Imgproc.minAreaRect(new MatOfPoint2f(bloco.toArray()));
			double comprimento = Math.max(r.size.width, r.size.height);
			double altura = Math.min(r.size.width, r.size.height);
			if (comprimento < larguraMinima || comprimento < altura * 5) {
				continue;
			}
			// O OpenCV mede o ângulo de um lado qualquer do retângulo; normalizamos para o lado comprido.
			double angulo = r.size.width >= r.size.height ? r.angle : r.angle - 90;
			if (angulo > 45) {
				angulo -= 90;
			} else if (angulo < -45) {
				angulo += 90;
			}
			angulos.add(angulo);
		}
		if (angulos.isEmpty()) {
			return 0;
		}
		Collections.sort(angulos);
		return angulos.get(angulos.size() / 2);
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
