package com.ewch.springboot.reactor.app;

import com.ewch.springboot.reactor.app.model.Comment;
import com.ewch.springboot.reactor.app.model.User;
import com.ewch.springboot.reactor.app.model.UserComment;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringbootReactorApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringbootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// iterableFlux();
		// flatmatExample();
		// flatmatFromUserToString();
		// collectMonoWithInnerList();
		// flatMapUserComment();
		// zipWithUserComment();
		// zipWithUserCommentWay2();
		// zipWithRange();
		// interval();
		// delayElements();
		// infiniteInterval();
		// infiniteIntervalFromCreate();
		// manualBackPressure();
		backPressure();
	}

	public void backPressure() {
		Flux.range(1, 10)
			.log()
			.limitRate(5)
			.subscribe();
	}

	public void manualBackPressure() {
		Flux.range(1, 10)
			.log()
			.subscribe(new Subscriber<Integer>() {

				private Subscription s;
				private Integer limit = 5;
				private Integer consumed = 0;

				@Override
				public void onSubscribe(Subscription s) {
					this.s = s;
					// s.request(Long.MAX_VALUE);
					s.request(limit);
				}

				@Override
				public void onNext(Integer integer) {
					LOGGER.info(integer.toString());
					consumed++;
					if (consumed == limit) {
						consumed = 0;
						s.request(limit);
					}
				}

				@Override
				public void onError(Throwable t) {

				}

				@Override
				public void onComplete() {

				}
			});
	}

	public void infiniteIntervalFromCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer count = 0;

				@Override
				public void run() {
					emitter.next(++count);
					if (count == 10) {
						timer.cancel();
						emitter.complete();
					}
					if (count == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, Flux has stopped in 5!"));
					}
				}
			}, 1000, 1000);
		})
		.subscribe(
				next -> LOGGER.info(next.toString()),
				error -> LOGGER.error(error.getMessage()),
				() -> LOGGER.info("Finished!")
			);
	}

	public void infiniteInterval() throws InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
			.doOnTerminate(countDownLatch::countDown)
			.flatMap(i -> {
				if (i >= 5) {
					return Flux.error(new InterruptedException("Count only until 5"));
				}
				return Flux.just(i);
			})
			.map(second -> "Hi in second " + second)
			.retry(2)
			// .doOnNext(LOGGER::info)
			.subscribe(LOGGER::info, error -> LOGGER.error(error.getMessage()));

		countDownLatch.await();
	}

	public void delayElements() throws InterruptedException {
		Flux<Integer> range = Flux.range(1, 12)
			.delayElements(Duration.ofSeconds(1))
			.doOnNext(item -> LOGGER.info(item.toString()));
		range.subscribe();
		Thread.sleep(13000);
	}

	public void interval() {
		Flux<Integer> range = Flux.range(1, 12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));
		range.zipWith(delay, (ran, del) -> ran)
			.doOnNext(item -> LOGGER.info(item.toString()))
		.blockLast();
	}

	public void zipWithRange() {
		Flux.just(1, 2, 3, 4, 5)
		.map(item -> item * 2)
		.zipWith(Flux.range(0, 5), (one, two) -> String.format("First Flux: %d :: Second Flux: %d", one, two))
		.subscribe(text -> LOGGER.info(text));
	}

	public void zipWithUserCommentWay2() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));
		Mono<Comment> commentMono = Mono.fromCallable(() -> {
			Comment comment = new Comment();
			comment.addComment("Comment 1");
			comment.addComment("Comment 2");
			comment.addComment("Comment 3");
			comment.addComment("Comment 4");
			return comment;
		});

		Mono<UserComment> userCommentMono = userMono
			.zipWith(commentMono)
			.map(tuple -> {
				User user = tuple.getT1();
				Comment comment = tuple.getT2();
				return new UserComment(user, comment);
			});
		userCommentMono.subscribe(userComment -> LOGGER.info(userComment.toString()));
	}

	public void zipWithUserComment() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));
		Mono<Comment> commentMono = Mono.fromCallable(() -> {
			Comment comment = new Comment();
			comment.addComment("Comment 1");
			comment.addComment("Comment 2");
			comment.addComment("Comment 3");
			comment.addComment("Comment 4");
			return comment;
		});

		Mono<UserComment> userCommentMono = userMono
			.zipWith(commentMono, (user, comment) -> new UserComment(user, comment));
		userCommentMono.subscribe(userComment -> LOGGER.info(userComment.toString()));
	}

	public void flatMapUserComment() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));
		Mono<Comment> commentMono = Mono.fromCallable(() -> {
			Comment comment = new Comment();
			comment.addComment("Comment 1");
			comment.addComment("Comment 2");
			comment.addComment("Comment 3");
			comment.addComment("Comment 4");
			return comment;
		});

		userMono.flatMap(user -> commentMono
			.map(comment -> new UserComment(user, comment)))
		.subscribe(userComment -> LOGGER.info(userComment.toString()));
	}

	public void collectMonoWithInnerList() {
		String names[] = {"Eimer Castro", "Alejandra Marin", "Manuel Castro", "Luisa Hincapie", "Duvan Castro", "Bruce Lee", "Bruce Willis"};
		List<User> namesList = Arrays.stream(names)
			.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
			.collect(Collectors.toList());

		Flux.fromIterable(namesList)
			.collectList()
			.subscribe(list -> {
				list.forEach(user -> LOGGER.info(user.toString()));
			});
	}

	public void flatmatFromUserToString() throws Exception {
		String names[] = {"Eimer Castro", "Alejandra Marin", "Manuel Castro", "Luisa Hincapie", "Duvan Castro", "Bruce Lee", "Bruce Willis"};
		List<User> namesList = Arrays.stream(names)
			.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
			.collect(Collectors.toList());

		Flux.fromIterable(namesList)
			// Using map operator to convert a String in a User object
			.map(user -> user.getFirstName().toUpperCase().concat(" ").concat(user.getLastName()).toUpperCase())
			// Using filter operator to get users with a pattern
			.flatMap(fullName -> {
				if (fullName.contains("Bruce".toUpperCase())) {
					return Mono.just(fullName);
				} else {
					return Mono.empty();
				}
			})
			// Using map operator to transform a User's attribute value into another one (String to lowercase)
			.map(String::toLowerCase)
			// Subscribing users because observables are immutable. To get a new data it's important create a new object.
			.subscribe(
				// Main method  to execute in callback
				user -> LOGGER.info(user.toString()),
				// Error handler
				error -> LOGGER.error(error.getMessage()),
				// onComplete method to execute when callback finishes
				() -> LOGGER.info("Execution has finished successfully!")
			);
	}

	public void flatmatFromStringToUser() throws Exception {
		String names[] = {"Eimer Castro", "Alejandra Marin", "Manuel Castro", "Luisa Hincapie", "Duvan Castro", "Bruce Lee", "Bruce Willis"};
		List<String> namesList = new ArrayList<>(Arrays.asList(names));

		Flux.fromIterable(namesList)
			// Using map operator to convert a String in a User object
			.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
			// Using filter operator to get users with a pattern
			.flatMap(user -> {
				if (user.getFirstName().equalsIgnoreCase("bruce")) {
					return Mono.just(user);
				} else {
					return Mono.empty();
				}
			})
			// Using map operator to transform a User's attribute value into another one (String to lowercase)
			.map(user -> {
				user.setFirstName(user.getFirstName().toLowerCase());
				return user;
			})
			// Subscribing users because observables are immutable. To get a new data it's important create a new object.
			.subscribe(
				// Main method  to execute in callback
				user -> LOGGER.info(user.toString()),
				// Error handler
				error -> LOGGER.error(error.getMessage()),
				// onComplete method to execute when callback finishes
				() -> LOGGER.info("Execution has finished successfully!")
			);
	}

	public void iterableFlux() throws Exception {
		String names[] = {"Eimer Castro", "Alejandra Marin", "Manuel Castro", "Luisa Hincapie", "Duvan Castro", "Bruce Lee", "Bruce Willis"};
		List<String> namesList = new ArrayList<String>(Arrays.asList(names));

		// Flux<String> namesStream = Flux.just(names);
		Flux<String> namesStream = Flux.fromIterable(namesList);

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
