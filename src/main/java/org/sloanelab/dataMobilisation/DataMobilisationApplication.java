package org.sloanelab.dataMobilisation;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DataMobilisationApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(DataMobilisationApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
		
	}

}
