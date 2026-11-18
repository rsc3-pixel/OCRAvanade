package br.com.ocravanade.envio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

	List<Envio> findTop50ByOrderByIdDesc();
}
