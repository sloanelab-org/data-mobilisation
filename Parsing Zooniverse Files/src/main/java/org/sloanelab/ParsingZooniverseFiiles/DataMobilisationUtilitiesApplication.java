package org.sloanelab.ParsingZooniverseFiiles;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DataMobilisationUtilitiesApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(DataMobilisationUtilitiesApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
		
	}

}
