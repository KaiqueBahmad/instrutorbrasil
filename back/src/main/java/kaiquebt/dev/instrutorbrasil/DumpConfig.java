package kaiquebt.dev.instrutorbrasil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@EnableAutoConfiguration(exclude = {
	DataSourceAutoConfiguration.class,
	SecurityAutoConfiguration.class,
	UserDetailsServiceAutoConfiguration.class,
	OAuth2ClientAutoConfiguration.class,
	MailSenderAutoConfiguration.class
})
public class DumpConfig {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(DumpConfig.class);
		app.setAdditionalProfiles("none");

		ConfigurableApplicationContext context = app.run(args);
		Environment env = context.getEnvironment();

		System.out.println("\n" + "=".repeat(80));
		System.out.println("APPLICATION.PROPERTIES VALUES");
		System.out.println("=".repeat(80) + "\n");

		printProperty(env, "spring.application.name");
		printProperty(env, "server.port");
		System.out.println();
		printProperty(env, "spring.datasource.url");
		printProperty(env, "spring.datasource.username");
		printProperty(env, "spring.datasource.password");
		System.out.println();
		printProperty(env, "jwt.secret");
		printProperty(env, "jwt.access-token-expiration");
		printProperty(env, "jwt.refresh-token-expiration");
		System.out.println();
		printProperty(env, "spring.mail.host");
		printProperty(env, "spring.mail.port");
		printProperty(env, "spring.mail.username");
		printProperty(env, "spring.mail.password");
		System.out.println();
		printProperty(env, "app.email.from");
		printProperty(env, "app.email.from-name");
		printProperty(env, "app.frontend.url");
		System.out.println();
		printProperty(env, "spring.security.oauth2.client.registration.google.client-id");
		printProperty(env, "spring.security.oauth2.client.registration.google.client-secret");

		System.out.println("\n" + "=".repeat(80) + "\n");

		context.close();
	}

	private static void printProperty(Environment env, String key) {
		String value = env.getProperty(key);
		System.out.println(key + "=" + (value != null ? value : "NOT SET"));
	}
}
