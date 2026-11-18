package br.com.ocravanade.envio;

import br.com.ocravanade.extracao.DadosDocumento;
import br.com.ocravanade.validacao.RegraAplicada;
import java.time.Instant;
import java.util.List;

/**
 * O que a API devolve. Separado da entidade para o banco poder mudar sem quebrar o front.
 * O CPF sai mascarado: dado pessoal não trafega inteiro sem necessidade (LGPD).
 */
public record EnvioResposta(
		Long id,
		String nomeArquivo,
		TipoDocumento tipo,
		StatusEnvio status,
		String extrator,
		String textoExtraido,
		DadosDocumento dados,
		List<RegraAplicada> regras,
		Instant criadoEm) {

	static EnvioResposta de(Envio envio) {
		DadosDocumento d = envio.getDados();
		DadosDocumento mascarado = new DadosDocumento(d.nome(), mascarar(d.cpf()), d.endereco(), d.cidade(),
				d.estado(), d.dataEmissao());
		return new EnvioResposta(envio.getId(), envio.getNomeArquivo(), envio.getTipo(), envio.getStatus(),
				envio.getExtrator(), envio.getTextoExtraido(), mascarado,
				envio.getRegras().stream().map(RegraVerificada::paraRegraAplicada).toList(), envio.getCriadoEm());
	}

	private static String mascarar(String cpf) {
		return cpf == null || cpf.length() != 11 ? cpf : cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
	}
}
