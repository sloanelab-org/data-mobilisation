package org.sloanelab.IntegratingReviewersAnnotations;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class IntegratingReviewersAnnotationsApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(IntegratingReviewersAnnotationsApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
		
	}

}
