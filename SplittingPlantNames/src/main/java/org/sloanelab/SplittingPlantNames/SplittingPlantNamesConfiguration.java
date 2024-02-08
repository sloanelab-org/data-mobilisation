package org.sloanelab.SplittingPlantNames;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SplittingPlantNamesConfiguration {
	
	@Value("${authoritiesFilePath}")
	String authoritiesFilePath;
	
	
	@Value("${spellCheckLookupFilePath}")
	String spellCheckLookupFilePath;
	
	@Bean
	@Primary
	public Map<String, String> authorities() throws FileNotFoundException {
		
		Map<String, String> authorities = new HashMap<String, String>();

	    Scanner reader = new Scanner(new File(authoritiesFilePath));
	    while (reader.hasNextLine()) {
	    	String[] lineParts = reader.nextLine().split("##");
	    	authorities.put(lineParts[0].replace("s", "[f|s]").replace("h", "[b|h]").replace(".", "[.]?").trim(),
	    			        lineParts[1].trim());
	    }
	    
		reader.close();
		
		return authorities;
		
	}
	
	
	@Bean
	public Map<String, String> getSpellCheckLookup() throws FileNotFoundException {
		
		Map<String, String> lookup = new HashMap<String, String>();

	    Scanner reader = new Scanner(new File(spellCheckLookupFilePath));
	    while (reader.hasNextLine()) {
	    	String[] lineParts = reader.nextLine().split("##");
	    	lookup.put(lineParts[0].trim(),lineParts[1]);
	    }
	    
		reader.close();
		
		return lookup;
		
	}

}
