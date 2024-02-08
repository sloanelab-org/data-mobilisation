package org.sloanelab.SplittingPlantNames.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sloanelab.SplittingPlantNames.model.SinglePlantName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class PlantNameSplitting {
	
	
	
	@Autowired
	Map<String,String> authorities;
	
	
	@Autowired
	@Qualifier("getSpellCheckLookup")
	Map<String,String> spellCheckLookup;
	
	String stopWords = "(lib|[?]|exot).*";
	
	public void splitAllPlantNames (String inputFile) throws Exception {
		
        ObjectMapper mapper = new ObjectMapper();
        
        JsonNode root = mapper.readTree(new File(inputFile));
        
        for (JsonNode node:root)  {
        	    
	            for (JsonNode plantNode:node.get("plants")) {

	            	ObjectNode detailsNode = mapper.createObjectNode();
	       
	            	String plantNames;
	            	
	            	if (plantNode.has("textV2"))
	            		 plantNames = plantNode.get("textV2").asText();
	            	else plantNames = plantNode.get("text").asText();
	            		            	
	            	Matcher match = Pattern.compile("[A-Z]?[.]? ?[*]? ?[IO0-9]*[.]? ").matcher(plantNames);
	            	
	                if (match.find() && match.start() == 0) {
	                  detailsNode.put("entry", match.group().trim());
	                  plantNames = plantNames.substring(match.end());
	                }
	                plantNames = plantNames.trim();
	                plantNames = plantNames + " ";
	                
	                ArrayList<SinglePlantName> singlePlantNames = splitPlantName(plantNames);

	            	ArrayNode namesNode = detailsNode.putArray("names");
	            	for (SinglePlantName singleName:singlePlantNames) {
	            		ObjectNode singleNameNode = mapper.createObjectNode();
	            		singleNameNode.put("name", singleName.getName());
	            		ArrayNode authorsNode = singleNameNode.putArray("authors");
	            		for (String author:singleName.getAuthors())
	            			authorsNode.add(author);
	            		singleNameNode.put("type", singleName.getType());
	            		
	            		namesNode.add(singleNameNode);
	            		
	            	}
	            	
	            	
	            	if (plantNode.has("details"))
	            		((ObjectNode) plantNode).replace("details",detailsNode);
	            	else
	            	  ((ObjectNode) plantNode).putPOJO("details", detailsNode);
	            	
	            }
            }
        
        mapper.writeValue(new File(new File(inputFile).getParent()+"/SplitResult.json"), root);
        
	} 
	
	
	public ArrayList<SinglePlantName> splitPlantName (String plantNames) {
		
		ArrayList<String> authorsAbbrev = new ArrayList<>(authorities.keySet());
		
		authorsAbbrev.sort((s1, s2) -> s2.replace("[f|s]","f").replace("[b|h]","b").replace(" ","").replace("[.]?","").replace("?", "").length() - 
                s1.replace("[f|s]","f").replace("[b|h]","b").replace(" ", "").replace("[.]?", "").replace("?","").length());
    			

		String authorsRegex = "\\b\\d*[.]?\\s?(" + String.join("|", authorsAbbrev) + ")[.|,|;|\\?]?[A-Z\\s]";
		
		Pattern authorsPattern = Pattern.compile(authorsRegex);
		
		plantNames = normalise(plantNames);
		
		plantNames = spellCheckLookup(plantNames);

		plantNames = seprateAuthorNames(plantNames,authorsAbbrev);
		  	
		Matcher matcher = authorsPattern.matcher(plantNames);
	
        int counter = 1;   
        
        int position = 0;
        
        ArrayList<SinglePlantName> singlePlantNames = new ArrayList<>();
        
        SinglePlantName singlePlantName;
	
    	while (matcher.find()) {    
    		    String author = matcher.group();
        		if (position == matcher.start() -1) {        			
        			SinglePlantName lastSingleName = singlePlantNames.get(singlePlantNames.size()-1);
        			ArrayList<String> lastAuthors = lastSingleName.getAuthors();
        			lastAuthors.add(spellCheckAuthor(author.substring(0,author.length()-1)));
        			
        			position = matcher.end()-1;
        			continue;
        		}
    		
            	singlePlantName = new SinglePlantName();
            	singlePlantName.setName(cleanPlantName(plantNames.substring(position,matcher.start()).trim()));
            	singlePlantName.getAuthors().add(spellCheckAuthor(author.substring(0,author.length()-1)));
        	
        		if (counter++ > 1)
        		     singlePlantName.setType("Synonym");
        		else 
        			singlePlantName.setType("Accepted");
        	
        		singlePlantNames.add(singlePlantName);
        		
        		position = matcher.end()-1;
    	}
	
    	if (plantNames.length()>position + 1 && ! plantNames.substring(position).matches(stopWords)) {  
        	  singlePlantName = new SinglePlantName();
        	  singlePlantName.setName(cleanPlantName(plantNames.substring(position).trim()));
        	  if (counter>1)
        		  singlePlantName.setType("Common");
        	  else 
        		  singlePlantName.setType("Accepted");
        	  singlePlantNames.add(singlePlantName);
    	} 
		
    	
    	
    	fixImplicitGenericNames(singlePlantNames);

		return singlePlantNames;
	
	}
	
	
	private String cleanPlantName(String plantName) {
		
		String pattern = "([A-Z]\\.\\s|\\d+\\.\\d+|\\d+\\.\\s)";
	
		plantName = plantName.replaceAll(pattern,"");

		return plantName;
	}
	
	private void fixImplicitGenericNames(ArrayList<SinglePlantName> singlePlantNames) {
		
		String genericName = null;
		
		for (SinglePlantName singlePlantName:singlePlantNames) {
		
			if (singlePlantName.getType().equals("Accepted") 
				&& singlePlantName.getName().contains(" ")
				&& singlePlantName.getName().split(" ")[0].length() > 0
			    && Character.isUpperCase(singlePlantName.getName().split(" ")[0].charAt(0)))
				
				    genericName = singlePlantName.getName().split(" ")[0];
			   
			else if (singlePlantName.getType().equals("Synonym") && genericName !=null
					 && singlePlantName.getName().contains(" ")
					 && !Character.isUpperCase(singlePlantName.getName().split(" ")[0].charAt(0)))
					
				  singlePlantName.setName(genericName + " " + singlePlantName.getName());
			
		}
	}


	private String spellCheckLookup(String plantNames) {
		for (Map.Entry<String, String> entry: spellCheckLookup.entrySet()) 
			plantNames = plantNames.replaceAll(entry.getKey(),entry.getValue());
			
		return plantNames;
	}



	private String spellCheckAuthor(String author) {
		if (author.toLowerCase().contains("luf"))
			author = author.replace("luf", "lus");
		if(author.contains("Morif"))
			author = author.replace("Morif", "Moris");
		
		return author.trim();
		
	}	
	
    private String seprateAuthorNames(String input, ArrayList<String> authorsAbbrev) {    	
    	
    	String replacement = "$1 ";
    	Pattern regexPattern = Pattern.compile("(\\b(" + String.join("|", authorsAbbrev) + ")(\\.|,|;|\\?|\\s|[A-Z]|$))(?!\\s)");
    
        Matcher matcher = regexPattern.matcher(input);
        String result = matcher.replaceAll(replacement);
                      
		result = result.replaceAll("\\s+", " ");
		
        return result.toString();
    }
	
	private String normalise (String input) {
		
		input = input.replaceAll(" \\?", "?");
		input = input.replaceAll("®", "");
		input = input.replaceAll("- ", "");
		
		
		return input;
	}
	
	
}
