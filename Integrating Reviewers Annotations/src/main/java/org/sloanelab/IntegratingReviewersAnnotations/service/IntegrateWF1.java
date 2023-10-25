package org.sloanelab.IntegratingReviewersAnnotations.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class IntegrateWF1 {
	
	static ObjectMapper mapper = new ObjectMapper();
	
	public static void consolidateWF1(String annotationsFilePath, String consolidatedFilePath,String vol) throws Exception {
		
		ArrayNode zooniNodes = (ArrayNode) mapper.readTree(new File(annotationsFilePath));
		
		ArrayNode textractNodes = (ArrayNode) mapper.readTree(new File(consolidatedFilePath));
		
		for (JsonNode zooniNode: zooniNodes) {
			String id = zooniNode.get("id").asText();
			for (JsonNode textractNode: textractNodes) 
				if(textractNode.get("pageNo").asText().equals(id)) {
					String prefix = vol + "-" + textractNode.get("pageNo").asText();
					((ObjectNode) textractNode).replace("pageNo",zooniNode.get("pageNo"));
					consolidateWF1NewSegments((ArrayNode)zooniNode.get("newSegments"), (ObjectNode)textractNode,prefix);
				}
		}
			    
		mapper.writeValue(new File (consolidatedFilePath),textractNodes);
		
	}


	private static void consolidateWF1NewSegments(ArrayNode zooniNewSegments, ObjectNode textractNode, String prefix) {
		for (JsonNode zooniNewSegment:zooniNewSegments) {	
			if (zooniNewSegment.get("width").asInt() > 2600 && zooniNewSegment.get("y").asInt() < 1500)
				   textractNode.putPOJO("header", creatNoPlantNodes(zooniNewSegment,prefix+"-header"));
			else if (zooniNewSegment.get("width").asInt() > 2600 && zooniNewSegment.get("y").asInt() > 4500)
					textractNode.putPOJO("footer", creatNoPlantNodes(zooniNewSegment,prefix+"-footer"));	
			else 
				addPlantNode(zooniNewSegment,textractNode,prefix);
		}
	}


	private static void addPlantNode(JsonNode zooniNewSegment,ObjectNode textractNode,String prefix) {
		ArrayNode plantsNode = (ArrayNode) textractNode.get("plants");
		ObjectNode newPlantNode = new ObjectMapper().createObjectNode();
		int plantNo = plantsNode.size() + 1;
		newPlantNode.put("id", prefix+"-"+plantNo+"-plant");
		newPlantNode.put("text", "");
		newPlantNode.put("left", zooniNewSegment.get("x").asInt());
		newPlantNode.put("top",zooniNewSegment.get("y").asInt());
		newPlantNode.put("width",zooniNewSegment.get("width").asInt());
		newPlantNode.put("height", zooniNewSegment.get("height").asInt());
		plantsNode.add(newPlantNode);
	    textractNode.replace("plants", plantsNode);
	}

		
	private static ObjectNode creatNoPlantNodes(JsonNode zooniNode,String id) {
		      ObjectMapper mapper = new ObjectMapper();
		      ObjectNode newNode = mapper.createObjectNode();
		      newNode.put("id", id);
	          newNode.put("left", zooniNode.get("x").asInt());
	          newNode.put("top", zooniNode.get("y").asInt());
	          newNode.put("width", zooniNode.get("width").asInt());
	          newNode.put("height", zooniNode.get("height").asInt());
	          newNode.put("text", "");
	          return newNode;
    }
	
}





