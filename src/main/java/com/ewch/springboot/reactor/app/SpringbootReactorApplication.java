package com.ewch.springboot.reactor.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringbootReactorApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringbootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String names[] = {"Eimer", "Alejandra", "Manuel", "Luisa", "Duvan"};
		Flux<String> namesStream = Flux.just(names)
			.doOnNext(name -> {
				if(name.isEmpty()) {
					throw new RuntimeException("Names cannot be empty.");
				}
				System.out.println(name);
			});

		namesStream.subscribe(
			name -> LOGGER.info(name),
			error -> LOGGER.error(error.getMessage()),
			() -> LOGGER.info("Execution has finished successfully!")
		);
	}
}
