package org.sloanelab.SpellChecker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpellCheckerApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(SpellCheckerApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
		
	}

}
