package org.sloanelab.SplittingPlantNames;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SplittingPlantNamesApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(SplittingPlantNamesApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
		
	}

}
