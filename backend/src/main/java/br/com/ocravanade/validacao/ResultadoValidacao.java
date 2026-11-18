package br.com.ocravanade.validacao;

import java.util.List;

public record ResultadoValidacao(boolean aprovado, List<RegraAplicada> regras) {
}
