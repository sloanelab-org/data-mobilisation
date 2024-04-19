package org.sloanelab.SpellChecker.service;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Set;
import java.io.IOException;
import java.util.ArrayList;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
public class SpellCheckingPlantNames {
	
	int threshold = 85;
	
	
	String confidence = "";
	
	int matchedWordCounter = 0;
	

	@Autowired
	Set<String> species;
		
	@Autowired
	@Qualifier("getInfraSpecies")
	Set<String> infraspecies;
	
	
	@Autowired
	@Qualifier("getNHMPortalNames")
	Set<String> NHMPortalNames;
	
	@Autowired
	@Qualifier("gateChatGPTRestTemplate")
	RestTemplate restChatGPTTemplate;

	String stopWords = "(lib|[?]|exot).*";
	
	
	public void spellCheckPlantNameFile (String inputFilePath, String outputFilePath, int startNode) throws Exception {
		
		int counter = 0;
		
		species.addAll(infraspecies);
		species.addAll(NHMPortalNames);
		
		
	    ObjectMapper mapper = new ObjectMapper();
	    
	    if (! new File(outputFilePath).exists() || new File(outputFilePath).length() == 0) {
	          FileWriter myWriter = new FileWriter(outputFilePath);
	          myWriter.write("[]");
	          myWriter.close();
	    }
	    
	    ArrayNode outputRoot = (ArrayNode) mapper.readTree(new File(outputFilePath));
	    
		JsonNode inputRoot = mapper.readTree(new File (inputFilePath));
		
		for (JsonNode inputNode:inputRoot) {
			if (counter++ < startNode) continue;
			
			System.out.println("Counter:" + counter);
			for (JsonNode plant:inputNode.path("plants")) 
				for (JsonNode name: plant.path("details").path("names")) {
						String spellCheckedPlantName = spellCheckPlantName(name.get("name").asText());
						((ObjectNode) name).put("name", spellCheckedPlantName);
						((ObjectNode) name).put("confidence", gradeConfidence(spellCheckedPlantName,plant.get("confidence"))); 					
				}	
			outputRoot.add(inputNode); 
			mapper.writeValue(new File(outputFilePath), outputRoot);
		}
		
	}
	
	public String spellCheckPlantName(String plantName) throws Exception {
		matchedWordCounter = 0;
		String[] plantWords = plantName.split(" ");
    	List<String> checkedPlantName = new ArrayList<String>();
    	for (String plantWord:plantWords) {
    		   if (plantWord.replace(".", "").length() < 3 || plantWord.matches(stopWords)) {
    			   checkedPlantName.add(plantWord);
    			   matchedWordCounter ++;
    		   }
    		   else if (dictionariesMatch(checkedPlantName,plantWord)) {
    			   matchedWordCounter ++;
    			   continue;
    		   }

    		   else if (speciesMatch(checkedPlantName,plantWord)) {
    			   matchedWordCounter ++;
    			   continue;
    		   }
    		   
    		   else checkedPlantName.add(plantWord);
    	}		   
    	

    	return String.join(" ",checkedPlantName);

	}
	
	
	public  String gradeConfidence(String plantName, JsonNode confidence)  {
		
		double score = 0;
		int matchcounter = 0;
		
		String[] plantWords = plantName.split(" ");
		
		for (String word:plantWords) 
			if (confidence != null &&  confidence.has(word)) {
			    score += confidence.get(word).asDouble();
			    matchcounter ++;
			}
		
		score = score / matchcounter;
		
    	if ((plantWords.length <= 2 && score >= 60)
    			|| (double) matchedWordCounter / plantWords.length >= 0.6) 
    		 return "High";
		
		else if ((plantWords.length <= 2 && score >= 30)
				|| (double) matchedWordCounter / plantWords.length >= 0.3) 
			return "Medium";
    	
		else
			return "Low";
		
	}


    public static boolean dictionariesMatch (List<String> checkedPlantName, String plantWord) throws Exception {
            	
    	List<String> wordVariations = new ArrayList<>();
    	List<String> tempList = new ArrayList<>();
    	plantWord = plantWord.replace("√≥", "o").replace("(", "s").replace("/", "s").replace(" b ", "&");
    	wordVariations.add(plantWord);
    	
    	if (plantWord.contains("f"))
      	     wordVariations.add(plantWord.replace('f', 's'));
    	
    	if(plantWord.startsWith("F") )
    		wordVariations.add(plantWord.replaceFirst("F", "J"));
    	
    	
    	if (plantWord.contains("uu")) {
    		wordVariations.forEach(word -> {tempList.add(word.replace("uu","vu"));});
    		wordVariations.addAll(tempList);
    		tempList.clear();
    	}
    	
    	if (plantWord.contains("b")) {
    		wordVariations.forEach(word -> {tempList.add(word.replace("b","h"));
    		                                tempList.add(word.replace("b","ch"));});
    		wordVariations.addAll(tempList);
    		tempList.getClass();
    		
    	}  	
    	if (plantWord.contains("x")) {
    		wordVariations.forEach(word -> {tempList.add(word.replace('x','z'));
    		                                tempList.add(word.replace("x","ae"));});
    		
    		wordVariations.addAll(tempList);
    		tempList.clear();
    	}
    	
      	// fix words like AEthiopicum
    	if (plantWord.startsWith("A"))
    		wordVariations.add(plantWord.replace("A", "AE"));
    	
         	
    	if (plantWord.contains("g")) {
    		wordVariations.forEach(word -> {tempList.add(word.replace("g","q"));});
    		wordVariations.addAll(tempList);
    		tempList.clear();
    	}
    	
    	
    	if (plantWord.contains("r")) {
    		wordVariations.forEach(word -> {tempList.add(word.replace("r","v"));});
    		wordVariations.addAll(tempList);
    		tempList.clear();
    	}
    	
    	if (plantWord.contains("l")) {
    		wordVariations.forEach(word -> {tempList.add(word.replace("l","f"));});
    		wordVariations.addAll(tempList);
    		tempList.clear();
    	}
    	
    	if (plantWord.endsWith("a")) {
    		wordVariations.forEach(word -> {tempList.add(word + "e");});
    		wordVariations.addAll(tempList);
    		tempList.clear();
    	}
    	
    		
    	for (String wrd:wordVariations) {
    		
    		String temp = wrd.toLowerCase().replace(".", "").replace(",", "");
    		    	    	
    		
    		if (wikiMatch(temp) || latinDicMatch(temp)) {
    			checkedPlantName.add(wrd);
    			return true;
    		}
    			
    	}
    	
       return false;
	   }
    
    
    public boolean speciesMatch (List<String> processedPlant, String plantWord) throws Exception {
    	
     	
        ExtractedResult matchingResult = FuzzySearch.extractOne(plantWord.toLowerCase(),species);
        
        if (matchingResult.getScore() == 100) {
        	processedPlant.add(plantWord);
            return true;
        }
        
        int matchingScore = FuzzySearch.ratio(matchingResult.getString(),plantWord.toLowerCase());
        

        if (matchingScore >= threshold && matchingResult.getString().length()>=plantWord.length()) {
        	processedPlant.add(matchingResult.getString());
        	return true;
        }
        

       return false;
       
	   }
    
    
    

	public static boolean wikiMatch (String word) throws Exception {
		
		RestTemplate restTemplate = new RestTemplate();
	
		try {
			
			String respond = restTemplate.getForObject("https://en.wiktionary.org/w/api.php?action=query&prop=revisions&titles="+word.toLowerCase()
			                                            +"&rvslots=*&rvprop=content&formatversion=2&format=json", String.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root   = mapper.readTree(respond);
		    
		    if (!root.has("query") || root.path("query").get("pages").get(0).has("missing"))
		    	return false;
		    		   
			for (JsonNode page:root.path("query").path("pages")) 
				if(page.has("revisions"))
					for (JsonNode revision:page.get("revisions")) 
						if(revision.path("slots").path("main").get("content").asText().matches(".*(==Latin==|species|genus).*")		
						  ||revision.path("slots").path("main").get("content").asText().contains("species")
						  ||revision.path("slots").path("main").get("content").asText().contains("genus"))
							 return true;
						 
			return false;
		
		
		}
			catch (Exception e) {
				e.printStackTrace();
				return false;
		}
		
		
	   }
	
	
	public static boolean latinDicMatch (String word) throws IOException {	
		
			
		try {
			
			
		    CloseableHttpClient httpClient = HttpClients.createDefault();
		    
		    String url = "https://www.online-latin-dictionary.com/latin-english-dictionary.php?parola=" + word;
		    
	        HttpGet getRequest  = new HttpGet(url);
	    
	        CloseableHttpResponse response = httpClient.execute(getRequest);

	        Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", url);
	        	        
	        if (document.text().contains("did not return any results"))
	        	return false;
	        else 
	        	return true;

		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	   }
	


   	
}
