package br.com.lucascoliveira.atlas.core;

import org.springframework.boot.SpringApplication;

public class TestAtlasCoreApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(AtlasCoreApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
