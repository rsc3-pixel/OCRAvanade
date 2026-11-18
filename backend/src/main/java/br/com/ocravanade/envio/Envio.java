package br.com.ocravanade.envio;

import br.com.ocravanade.extracao.DadosDocumento;
import br.com.ocravanade.validacao.ResultadoValidacao;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Etapa 6: cada documento processado vira uma linha no banco, com o texto, os dados e o veredito. */
@Entity
@Table(name = "envios")
public class Envio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nomeArquivo;

	@Enumerated(EnumType.STRING)
	private TipoDocumento tipo;

	@Enumerated(EnumType.STRING)
	private StatusEnvio status;

	private String extrator;

	@Column(length = 20_000)
	private String textoExtraido;

	private String nome;
	private String cpf;
	private String endereco;
	private String cidade;
	private String estado;
	private LocalDate dataEmissao;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "envio_regras", joinColumns = @JoinColumn(name = "envio_id"))
	@OrderColumn(name = "ordem")
	private List<RegraVerificada> regras = new ArrayList<>();

	private Instant criadoEm;

	protected Envio() {
	}

	static Envio processado(String nomeArquivo, TipoDocumento tipo, String extrator, String texto,
			DadosDocumento dados, ResultadoValidacao resultado, Instant agora) {
		Envio envio = new Envio();
		envio.nomeArquivo = nomeArquivo;
		envio.tipo = tipo;
		envio.extrator = extrator;
		envio.textoExtraido = texto;
		envio.nome = dados.nome();
		envio.cpf = dados.cpf();
		envio.endereco = dados.endereco();
		envio.cidade = dados.cidade();
		envio.estado = dados.estado();
		envio.dataEmissao = dados.dataEmissao();
		envio.status = resultado.aprovado() ? StatusEnvio.APROVADO : StatusEnvio.REPROVADO;
		resultado.regras().forEach(r -> envio.regras.add(new RegraVerificada(r)));
		envio.criadoEm = agora;
		return envio;
	}

	public Long getId() {
		return id;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public TipoDocumento getTipo() {
		return tipo;
	}

	public StatusEnvio getStatus() {
		return status;
	}

	public String getExtrator() {
		return extrator;
	}

	public String getTextoExtraido() {
		return textoExtraido;
	}

	public DadosDocumento getDados() {
		return new DadosDocumento(nome, cpf, endereco, cidade, estado, dataEmissao);
	}

	public List<RegraVerificada> getRegras() {
		return regras;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}
}
