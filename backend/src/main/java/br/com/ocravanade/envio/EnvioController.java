package br.com.ocravanade.envio;

import br.com.ocravanade.imagem.DocumentoInvalidoException;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {

	private final EnvioService servico;

	public EnvioController(EnvioService servico) {
		this.servico = servico;
	}

	@PostMapping(consumes = "multipart/form-data")
	@ResponseStatus(HttpStatus.CREATED)
	public EnvioResposta enviar(@RequestParam("arquivo") MultipartFile arquivo,
			@RequestParam(value = "tipo", defaultValue = "COMPROVANTE_RESIDENCIA") TipoDocumento tipo) throws IOException {
		if (arquivo.isEmpty()) {
			throw new DocumentoInvalidoException("Arquivo vazio");
		}
		return servico.processar(arquivo.getBytes(), arquivo.getOriginalFilename(), tipo);
	}

	@GetMapping
	public List<EnvioResposta> listar() {
		return servico.listar();
	}

	@GetMapping("/{id}")
	public EnvioResposta buscar(@PathVariable Long id) {
		return servico.buscar(id);
	}
}
