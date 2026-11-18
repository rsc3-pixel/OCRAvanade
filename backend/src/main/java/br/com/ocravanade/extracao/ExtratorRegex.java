package br.com.ocravanade.extracao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extração por palavras-chave + Regex. Funciona bem para layouts previsíveis.
 * O OCR erra acento e pontuação, por isso os padrões aceitam variações
 * (Endereco/Endereço, CPF com ou sem pontos).
 */
public class ExtratorRegex implements ExtratorDados {

	private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE;

	private static final Pattern NOME = Pattern.compile("^\\s*(?:nome|titular|cliente)\\s*[:\\-]\\s*(.+)$", FLAGS);
	private static final Pattern CPF = Pattern.compile("(\\d{3})\\.?\\s?(\\d{3})\\.?\\s?(\\d{3})\\s?[-.]?\\s?(\\d{2})");
	private static final Pattern ENDERECO = Pattern.compile("^\\s*endere[cç]o\\s*[:\\-]\\s*(.+)$", FLAGS);
	private static final Pattern CIDADE_UF = Pattern.compile("^\\s*([\\p{L} .']{2,}?)\\s*[-/]\\s*([A-Z]{2})\\s*$", Pattern.MULTILINE);
	private static final Pattern DATA_EMISSAO = Pattern.compile(
			"(?:data\\s+de\\s+emiss[aã]o|emiss[aã]o|emitido\\s+em)\\s*[:\\-]?\\s*(\\d{2}/\\d{2}/\\d{4})", FLAGS);

	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);

	@Override
	public DadosDocumento extrair(String textoOcr) {
		String texto = textoOcr == null ? "" : textoOcr;
		Matcher cidadeUf = CIDADE_UF.matcher(texto);
		boolean achouCidade = cidadeUf.find();
		return new DadosDocumento(
				primeiroGrupo(NOME, texto),
				cpf(texto),
				primeiroGrupo(ENDERECO, texto),
				achouCidade ? cidadeUf.group(1).trim() : null,
				achouCidade ? cidadeUf.group(2) : null,
				data(texto));
	}

	@Override
	public String nome() {
		return "regex";
	}

	private static String primeiroGrupo(Pattern padrao, String texto) {
		Matcher m = padrao.matcher(texto);
		return m.find() ? m.group(1).trim() : null;
	}

	private static String cpf(String texto) {
		Matcher m = CPF.matcher(texto);
		return m.find() ? m.group(1) + m.group(2) + m.group(3) + m.group(4) : null;
	}

	private static LocalDate data(String texto) {
		String bruta = primeiroGrupo(DATA_EMISSAO, texto);
		if (bruta == null) {
			return null;
		}
		try {
			return LocalDate.parse(bruta, DATA_BR);
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}
