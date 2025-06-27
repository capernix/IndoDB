package com.indodb.games_backend;

import org.springframework.boot.SpringApplication;

public class TestGamesBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(GamesBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
