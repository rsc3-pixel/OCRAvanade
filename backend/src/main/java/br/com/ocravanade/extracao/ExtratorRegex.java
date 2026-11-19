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

	private static final Pattern NOME = rotulo("nome(?:[ \\t]+do[ \\t]+(?:cliente|titular))?|titular|cliente");
	/** Só perto do rótulo: contas mostram o CPF mascarado, e sem isso o Regex pegava outro número de 11 dígitos. */
	private static final Pattern CPF = Pattern.compile(
			"cpf[ \\t]*[:.\\-]?[ \\t]*(\\d{3})\\.?\\s?(\\d{3})\\.?\\s?(\\d{3})\\s?[-.]?\\s?(\\d{2})", FLAGS);
	private static final Pattern ENDERECO = rotulo("endere[cç]o");
	private static final Pattern CEP_CIDADE_UF = Pattern.compile(
			"^[ \\t]*\\d{5}-?\\d{3}[ \\t]+([\\p{L} .']+?)[ \\t]+([A-Z]{2})[ \\t]*$", Pattern.MULTILINE);
	/** O "|" no fim é a borda da página lida pelo OCR. */
	private static final Pattern CIDADE_UF = Pattern.compile(
			"^\\s*([\\p{L} .']{2,}?)\\s*[-/]\\s*([A-Z]{2})[ \\t|]*$", Pattern.MULTILINE);
	private static final Pattern DATA_EMISSAO = Pattern.compile(
			"(?:data\\s+de\\s+emiss[aã]o|emiss[aã]o|emitido\\s+em)\\s*[:\\-]?\\s*(\\d{2}/\\d{2}/\\d{4})", FLAGS);

	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);

	@Override
	public DadosDocumento extrair(String textoOcr) {
		String texto = textoOcr == null ? "" : textoOcr;
		Matcher cidadeUf = primeiroQueAcha(texto, CEP_CIDADE_UF, CIDADE_UF);
		return new DadosDocumento(
				primeiroGrupo(NOME, texto),
				cpf(texto),
				primeiroGrupo(ENDERECO, texto),
				cidadeUf == null ? null : cidadeUf.group(1).trim(),
				cidadeUf == null ? null : cidadeUf.group(2),
				data(texto));
	}

	/**
	 * Valor na mesma linha ("Nome: Maria", "nome - Maria") ou, como em contas de luz, na linha de baixo
	 * de um rótulo sozinho ("NOME DO CLIENTE:" e o nome embaixo).
	 */
	private static Pattern rotulo(String rotulo) {
		return Pattern.compile("^[ \\t]*(?:" + rotulo + ")[ \\t]*(?:[:\\-][ \\t]*|:?[ \\t]*\\R\\s*)(\\S.*)$", FLAGS);
	}

	private static Matcher primeiroQueAcha(String texto, Pattern... padroes) {
		for (Pattern padrao : padroes) {
			Matcher m = padrao.matcher(texto);
			if (m.find()) {
				return m;
			}
		}
		return null;
	}

	@Override
	public String nome() {
		return "regex";
	}

	private static String primeiroGrupo(Pattern padrao, String texto) {
		Matcher m = padrao.matcher(texto);
		return m.find() ? m.group(1).trim().replaceAll("\\s+", " ") : null;
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
