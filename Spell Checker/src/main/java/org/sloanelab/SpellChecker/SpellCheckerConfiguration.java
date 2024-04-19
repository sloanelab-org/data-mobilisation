package org.sloanelab.SpellChecker;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpellCheckerConfiguration {
	
	@Value("${speciesFilePath}")
	String speciesFilePath;
	
	@Value("${infraSpeciesFilePath}")
	String infraSpeciesFilePath;
	
	@Value("${NHMPortalNamesFilePath}")
	String NHMPortalNamesFilePath;
	
	
	@Bean
	@Primary
	public Set<String> getSpecies() throws Exception {
		Set<String> species = new HashSet<>();
	    Scanner reader = new Scanner(new File(speciesFilePath));
	    while (reader.hasNextLine())
	      species.add(reader.nextLine());
		reader.close();
	    return species;
	}
	
	
	@Bean
	public Set<String> getInfraSpecies() throws Exception {
		Set<String> infraspecies = new HashSet<>();
	    Scanner reader = new Scanner(new File(infraSpeciesFilePath));
	    while (reader.hasNextLine())
	      infraspecies.add(reader.nextLine());
		reader.close();
	    return infraspecies;
	}
	
	
	
	@Bean
	public Set<String> getNHMPortalNames() throws Exception {
		Set<String> NHMPortalNames = new HashSet<>();
	    Scanner reader = new Scanner(new File(NHMPortalNamesFilePath));
	    while (reader.hasNextLine())
	      NHMPortalNames.add(reader.nextLine());
		reader.close();
	    return NHMPortalNames;
	}

	
	

}
