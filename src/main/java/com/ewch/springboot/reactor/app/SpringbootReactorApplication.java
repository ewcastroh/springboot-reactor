package com.ewch.springboot.reactor.app;

import com.ewch.springboot.reactor.app.model.User;
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
		String names[] = {"Eimer Castro", "Alejandra Marin", "Manuel Castro", "Luisa Hincapie", "Duvan Castro", "Bruce Lee", "Bruce Willis"};
		Flux<String> namesStream = Flux.just(names);
			// Using map operator to convert a String in a User object
		Flux<User> users = namesStream.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
			// Using filter operator to get users with a pattern
			.filter(user -> user.getFirstName().equalsIgnoreCase("Bruce"))
			.doOnNext(user -> {
				if(user == null) {
					throw new RuntimeException("Names cannot be empty.");
				}
				System.out.println(user.getFirstName().concat(" ").concat(user.getLastName()));
			})
			// Using map operator to transform a User's attribute value into another one (String to lowercase)
			.map(user -> {
				user.setFirstName(user.getFirstName().toLowerCase());
				return user;
				});

		// Subscribing namesStream because observables are immutable. To get a new data it's important create a new object.
		namesStream.subscribe(
			// Main method  to execute in callback
			user -> LOGGER.info(user.toString()),
			// Error handler
			error -> LOGGER.error(error.getMessage()),
			// onComplete method to execute when callback finishes
			() -> LOGGER.info("Execution has finished successfully!")
		);

		// Subscribing users because observables are immutable. To get a new data it's important create a new object.
		users.subscribe(
			// Main method  to execute in callback
			user -> LOGGER.info(user.toString()),
			// Error handler
			error -> LOGGER.error(error.getMessage()),
			// onComplete method to execute when callback finishes
			() -> LOGGER.info("Execution has finished successfully!")
		);
	}
}
