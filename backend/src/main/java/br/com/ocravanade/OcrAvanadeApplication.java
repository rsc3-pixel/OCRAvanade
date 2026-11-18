package br.com.ocravanade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OcrAvanadeApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcrAvanadeApplication.class, args);
	}
}
