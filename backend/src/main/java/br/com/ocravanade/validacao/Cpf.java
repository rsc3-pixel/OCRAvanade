package br.com.ocravanade.validacao;

/**
 * Regras do CPF no vocabulário do domínio. Os dois últimos dígitos são verificadores: calculados a partir dos
 * anteriores (pesos decrescentes, resto da divisão por 11). Se o OCR trocar um único dígito, a conta não fecha.
 */
public final class Cpf {

	private Cpf() {
	}

	public static boolean formatoValido(String digitos) {
		return digitos != null && digitos.matches("\\d{11}");
	}

	public static boolean valido(String digitos) {
		return formatoValido(digitos)
				&& !digitos.chars().allMatch(c -> c == digitos.charAt(0))
				&& digitoVerificador(digitos, 9) == digitos.charAt(9) - '0'
				&& digitoVerificador(digitos, 10) == digitos.charAt(10) - '0';
	}

	/** Nunca devolver o CPF completo na resposta: 123.***.***-09 */
	public static String mascarar(String digitos) {
		return formatoValido(digitos) ? digitos.substring(0, 3) + ".***.***-" + digitos.substring(9) : digitos;
	}

	private static int digitoVerificador(String digitos, int quantidade) {
		int soma = 0;
		for (int i = 0; i < quantidade; i++) {
			soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
		}
		int resto = soma % 11;
		return resto < 2 ? 0 : 11 - resto;
	}
}
