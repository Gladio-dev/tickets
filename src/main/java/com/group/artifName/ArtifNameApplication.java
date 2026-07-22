package com.group.artifName;

import com.group.artifName.services.EmailService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ArtifNameApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtifNameApplication.class, args);
	}

	@Bean
	CommandLineRunner init(
			BCryptPasswordEncoder encoder,
			EmailService emailService
	) {

		return args -> {


//			emailService.sendActivationEmail(null,"www.google.com");
//			emailService.sendTestEmail("alexis.castillo@rseguridad.com");
//			emailService.testAuthentication();
//			emailService.testToken();
			System.out.println("The App is running");


		};
	}
}