package org.sloanelab.IntegratingReviewersAnnotations.service;

import java.io.File;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class IntegrateWF2 {
	
		static ObjectMapper mapper = new ObjectMapper();
	

		public static void consolidateWF2(String annotationsFilePath, String consolidatedFilePath,String vol) throws Exception {
			
			ArrayNode zooniNodes = (ArrayNode) mapper.readTree(new File(annotationsFilePath));
			
			ArrayNode textractNodes = (ArrayNode) mapper.readTree(new File(consolidatedFilePath));
			
			for (JsonNode zooniNode: zooniNodes) {
				String id = zooniNode.get("pageNo").asText();
				for (JsonNode textractNode: textractNodes) 
					if(textractNode.get("pageNo").asText().equals(id)) {
						String prefix = vol + "-" + textractNode.get("pageNo").asText();
						consolidateIncorrectSegments((ArrayNode) zooniNode.get("incorrectSegments"), textractNode);
						consolidateTranscriptions((ArrayNode)zooniNode.get("transcriptions"), textractNode,prefix);
						consolidateWF2NewSegments((ArrayNode) zooniNode.get("newSegments"), (ObjectNode) textractNode,prefix);
						
					}
			}

		    mapper.writeValue(new File (consolidatedFilePath),removeEmptyBrackets(textractNodes));
			
		}
		
		
		protected static void consolidateIncorrectSegments(ArrayNode incorrectSegmentsNode, JsonNode textractNode) {
				
		    for (JsonNode incorrectSegmentNode:incorrectSegmentsNode) {         
		       if (incorrectSegmentNode.get("segment").asInt() == 0) {
		    	   String incorrectPlantId = incorrectSegmentNode.get("image").asText().split("plant")[0] + "plant";
			           for (JsonNode plantNode:textractNode.get("plants")) 
			        		if (! plantNode.isEmpty() &&  plantNode.get("id").asText().equals(incorrectPlantId)) 
			        			((ObjectNode) plantNode).removeAll();
			           for (JsonNode pairNode:textractNode.get("pairs")) 
			        		if (!pairNode.isEmpty() &&  pairNode.get("plant").asText().equals(incorrectPlantId)) {
			        			for (JsonNode marginNode:textractNode.get("margins"))
			        				if (! marginNode.isEmpty()&& marginNode.get("id").asText().
			        						equals(pairNode.get("margin").asText()))
			        					((ObjectNode) marginNode).removeAll();
			        			((ObjectNode) pairNode).removeAll();
			        		}
		       }
		     
		       else if (incorrectSegmentNode.get("segment").asInt() == 1) {
		    	   String incorrectMarginId = incorrectSegmentNode.get("image").asText().split("plant")[0] + "margin";   	   
		    	   for (JsonNode marginNode:textractNode.get("margins")) 
		        		if (!marginNode.isEmpty() &&  marginNode.get("id").asText().equals(incorrectMarginId)) 
		        			((ObjectNode) marginNode).removeAll();
		           for (JsonNode pairNode:textractNode.get("pairs")) 
		        		if (!pairNode.isEmpty() &&  pairNode.get("margin").asText().equals(incorrectMarginId)) 
		        			((ObjectNode) pairNode).removeAll();
		       }
		       
		
		       else if (incorrectSegmentNode.get("segment").asInt() == 2) 
		    	   if (textractNode.has("header"))
		               ((ObjectNode) textractNode).remove("header");
		
		
		       else if (incorrectSegmentNode.get("segment").asInt() == 3)
		    	   if (textractNode.has("footer"))
		    		   ((ObjectNode) textractNode).remove("footer");
		
		    }
		}
		
		
		protected static void consolidateTranscriptions(ArrayNode zooniTranscriptionsNode, JsonNode textractNode,String prefix) {
				
			zooniLoop:
			for (JsonNode zooniTranscriptionNode:zooniTranscriptionsNode) {
			   for (JsonNode plantNode:textractNode.get("plants"))
		            if(zooniTranscriptionNode.has("image") && ! plantNode.isEmpty() 
		            		&& zooniTranscriptionNode.get("image").asText().startsWith(plantNode.get("id").asText())) {
		
		            	for (JsonNode marginNode:textractNode.get("margins")) 
		            		if(!marginNode.isEmpty() && marginNode.get("id").asText().startsWith(plantNode.get("id").asText().split("-plant")[0])) {
		            			((ObjectNode) marginNode).replace("text",zooniTranscriptionNode.get("text"));
		            			if (zooniTranscriptionNode.has("x"))
		            				updateCoordinates((ObjectNode) marginNode,zooniTranscriptionNode);
		            		    continue zooniLoop;  	
		            		}
			   
			            int pairNo = Integer.valueOf(plantNode.get("id").asText().split("-")[2]);
		            	ArrayNode updatedMarginsNode = addSegmentNode(zooniTranscriptionNode,(ArrayNode) textractNode.get("margins"),prefix,"margin",pairNo);
					    ((ObjectNode) textractNode).replace("margins", updatedMarginsNode);
					    addPairNode((ObjectNode) textractNode,prefix,pairNo);
					    continue zooniLoop;
		             }
		            	
			}
			
			
		}
		
		
		protected static void consolidateCorrections(ArrayNode zooniCorrectionsNode, JsonNode textractNode,String prefix, String type) {
			
			for (JsonNode zooniCorrectionNode:zooniCorrectionsNode)
				if (type.equals("plants") && zooniCorrectionNode.has("image") && ! zooniCorrectionNode.get("image").asText().contains("plant")) {	
					int newSegNo =  textractNode.get("plants").size()+1;
					ArrayNode updatedPlantsNode = addSegmentNode(zooniCorrectionNode,(ArrayNode) textractNode.get("plants") ,prefix,"plant",newSegNo);
				   ((ObjectNode) textractNode).replace("plants", updatedPlantsNode);
				}
					
				else	{
					for (JsonNode segNode:textractNode.get(type)) 
						if(zooniCorrectionNode.has("image") && segNode.has("id") 
							&& segNode.get("id").asText().split("-"+type.substring(0,type.length()-1))[0].equals(zooniCorrectionNode.get("image").asText().split("-plant")[0]))
							updateCoordinates((ObjectNode) segNode,zooniCorrectionNode);
				}
		}
		
			
		private static void updateCoordinates(ObjectNode textractNode,JsonNode zooniNode) {
		          textractNode.replace("left", zooniNode.get("x"));
		          textractNode.replace("top", zooniNode.get("y"));
		          textractNode.replace("width", zooniNode.get("width"));
		          textractNode.replace("height", zooniNode.get("height"));
		}
		
		
		protected static void consolidateWF2NewSegments(ArrayNode newSegmentsNode, ObjectNode textractNode, String prefix) {
			
		
			 for (JsonNode newSegmentNode:newSegmentsNode) {
				 if (newSegmentNode.get("segment").asText().equals("plant")) {
					 
					int newSegNo =  textractNode.get("plants").size()+1;
					ArrayNode updatedPlantsNode = addSegmentNode(newSegmentNode,(ArrayNode) textractNode.get("plants") ,prefix,"plant",newSegNo);
				    textractNode.replace("plants", updatedPlantsNode);
				    
				    for (JsonNode newSegmentNode2:newSegmentsNode) 
				    	if (newSegmentNode2.get("segment").asText().equals("margin") && newSegmentNode2.get("No").equals(newSegmentNode.get("No"))) {
				    		ArrayNode updatedMarginsNode = addSegmentNode(newSegmentNode2,(ArrayNode) textractNode.get("margins"),prefix,"margin",newSegNo);
				    		textractNode.replace("margins", updatedMarginsNode);
				            addPairNode(textractNode,prefix,newSegNo);
				            break;
				    	}
				    	
				 }
		
			 }
			  
		}
		
		
		
		private static ArrayNode addSegmentNode(JsonNode zooniNewSegment, ArrayNode textractSegmentsNode,String prefix,String type,int segmentNo) {
			ObjectNode newSegmentNode = new ObjectMapper().createObjectNode();
			newSegmentNode.put("id", prefix+"-"+segmentNo+"-"+type);
			if (type.equals("plant"))
			    newSegmentNode.put("text", "");
			else newSegmentNode.put("text",zooniNewSegment.get("text").asText());
			try {
				newSegmentNode.put("left", zooniNewSegment.get("x").asInt());
				newSegmentNode.put("top",zooniNewSegment.get("y").asInt());
				newSegmentNode.put("width",zooniNewSegment.get("width").asInt());
				newSegmentNode.put("height", zooniNewSegment.get("height").asInt());
			textractSegmentsNode.add(newSegmentNode);
			return textractSegmentsNode;
			}
			catch (Exception e) {
				newSegmentNode.put("left", 0);
				newSegmentNode.put("top",0);
				newSegmentNode.put("width",0);
				newSegmentNode.put("height", 0);
				textractSegmentsNode.add(newSegmentNode);
				return textractSegmentsNode;
				
			}
		}
		
		
		private static void addPairNode(ObjectNode textractNode,String prefix,int pairNo) {
			
			ArrayNode pairsNode = (ArrayNode) textractNode.get("pairs");
			ObjectNode newPairNode = new ObjectMapper().createObjectNode();
			newPairNode.put("plant", prefix + "-" + pairNo + "-plant");
			newPairNode.put("margin", prefix + "-" + pairNo + "-margin");
			pairsNode.add(newPairNode);
			textractNode.replace("pairs", pairsNode);
		
		}
		
		
		private static ArrayNode removeEmptyBrackets(ArrayNode textractNodes) throws Exception {
			
			String content = textractNodes.toString();
					
			content = content.replaceAll("\\{\\},", "");
			
			content = content.replaceAll(",\\{\\}", "");
			
			content = content.replaceAll("\\[\\{\\}\\]","[]");
			
			ObjectMapper mapper = new ObjectMapper();
			
			return (ArrayNode) mapper.readTree(content);
			
		}

}
