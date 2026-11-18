package br.com.ocravanade.validacao;

import br.com.ocravanade.extracao.DadosDocumento;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Etapa 5: regras de negócio. Todas precisam passar para o documento ser aprovado.
 * O {@link Clock} é injetado para o teste conseguir fixar "hoje" e não quebrar com o passar dos dias.
 */
public class ValidadorDocumento {

	static final int MINIMO_CARACTERES_LEGIVEIS = 40;

	private final Clock relogio;
	private final int validadeDias;

	public ValidadorDocumento(Clock relogio, int validadeDias) {
		this.relogio = relogio;
		this.validadeDias = validadeDias;
	}

	public ResultadoValidacao validar(String textoOcr, DadosDocumento dados) {
		List<RegraAplicada> regras = List.of(
				legivel(textoOcr),
				presente("Nome encontrado", dados.nome()),
				cpf(dados.cpf()),
				presente("Endereço encontrado", dados.endereco()),
				emissaoRecente(dados.dataEmissao()));
		boolean aprovado = regras.stream().allMatch(RegraAplicada::aprovada);
		return new ResultadoValidacao(aprovado, regras);
	}

	private RegraAplicada legivel(String texto) {
		long uteis = texto == null ? 0 : texto.chars().filter(Character::isLetterOrDigit).count();
		boolean ok = uteis >= MINIMO_CARACTERES_LEGIVEIS;
		return new RegraAplicada("Documento legível", ok,
				ok ? uteis + " caracteres lidos" : "Pouco texto lido. Envie uma imagem nítida, sem cortes ou sombras");
	}

	private RegraAplicada presente(String regra, String valor) {
		boolean ok = valor != null && !valor.isBlank();
		return new RegraAplicada(regra, ok, ok ? valor : "Campo não encontrado no documento");
	}

	private RegraAplicada cpf(String cpf) {
		if (!Cpf.formatoValido(cpf)) {
			return new RegraAplicada("CPF válido", false, "CPF ausente ou incompleto");
		}
		boolean ok = Cpf.valido(cpf);
		return new RegraAplicada("CPF válido", ok, ok ? Cpf.mascarar(cpf)
				: "CPF " + Cpf.mascarar(cpf) + " com dígito verificador inválido. Confira se a imagem está nítida");
	}

	private RegraAplicada emissaoRecente(LocalDate emissao) {
		String regra = "Emissão nos últimos " + validadeDias + " dias";
		if (emissao == null) {
			return new RegraAplicada(regra, false, "Data de emissão não encontrada");
		}
		LocalDate hoje = LocalDate.now(relogio);
		if (emissao.isAfter(hoje)) {
			return new RegraAplicada(regra, false, "Data de emissão no futuro: " + emissao);
		}
		long dias = ChronoUnit.DAYS.between(emissao, hoje);
		boolean ok = dias <= validadeDias;
		return new RegraAplicada(regra, ok,
				ok ? "Emitido há " + dias + " dias" : "Documento antigo: emitido há " + dias + " dias");
	}
}
