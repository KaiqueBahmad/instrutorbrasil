package kaiquebt.dev.instrutorbrasil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InstrutorbrasilApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstrutorbrasilApplication.class, args);
	}

}
