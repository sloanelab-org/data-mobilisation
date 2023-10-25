package org.sloanelab.IntegratingReviewersAnnotations.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;

import org.sloanelab.IntegratingReviewersAnnotations.model.ZooniAnnotatedData;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.separator.SuffixRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

 public class ParseZooniAnnotations {
	
     
		public static ObjectNode parsePageNoTask (ObjectNode newNode ,JsonNode annotationsNode, JsonNode subjectNode, int taskNo) {
			newNode.put("id", subjectNode.get("pageNo").asText());
			if (!annotationsNode.get(taskNo).get("value").asText().equals("")) {
				newNode.put("pageNo", annotationsNode.get(taskNo).get("value").asText());	
			}
			else 
			   newNode.put("pageNo", subjectNode.get("pageNo").asText());
			return newNode;
		}


		public static ObjectNode parseTranscriptionTask (ObjectNode newNode ,JsonNode annotationsNode, JsonNode subjectNode, int taskNo) {
		
			 ObjectMapper mapper = new ObjectMapper();
			 
			 ArrayNode transcriptionsNode = newNode.putArray("transcriptions");
			 ArrayNode invalidSegementsNode = newNode.putArray("invalidSegments");
			 
			 for(JsonNode value:annotationsNode.get(taskNo).get("value")) {
				 
				 ObjectNode transcriptionNode = mapper.createObjectNode();
				 ObjectNode invalidSegmentNode = mapper.createObjectNode();
		
				 String imageId;
				 
				 if (value.get("frame").asText().equals("0"))
					 imageId = subjectNode.get("image").asText();
				 else			 
				     imageId = subjectNode.get("img"+value.get("frame").asText()).asText();
				 
				 if (value.get("tool").asInt() == 2) {
					 invalidSegmentNode.put("image", imageId);
					 invalidSegmentNode.put("x1",value.get("x1").asInt());
					 invalidSegmentNode.put("y1",value.get("y1").asInt());
					 invalidSegmentNode.put("x2",value.get("x2").asInt());
					 invalidSegmentNode.put("y2",value.get("y2").asInt());	
					 invalidSegementsNode.add(invalidSegmentNode);
				 }
				 
			    if (value.get("tool").asInt() == 1) {
		             transcriptionNode.put("x",value.get("x").asInt());
		             transcriptionNode.put("y",value.get("y").asInt());
		             transcriptionNode.put("width",value.get("width").asInt());
		             transcriptionNode.put("height",value.get("height").asInt());
				}
			    
			    if (value.get("tool").asInt() != 2) {
			        String transcription =  "@:" + value.get("details").get(0).get("value").asText();
			        transcriptionNode.put("image", imageId); 		    
				    transcriptionNode.put("text", transcription);
			    }
			    
				transcriptionsNode.add(transcriptionNode);
				
			}
			return newNode;
		}
		public static ObjectNode parseMissingSegmentsTool (ObjectNode newNode ,JsonNode annotations, int toolNo) {
			
			ObjectMapper mapper = new ObjectMapper();
			
			ArrayNode newSegmentsNode = newNode.putArray("newSegments");
			
			for(JsonNode value:annotations.get("value"))
			    if (value.get("tool").asInt() == toolNo) {
			    	 ObjectNode innerNode = mapper.createObjectNode();
		             innerNode.put("x",value.get("x").asInt());
		             innerNode.put("y",value.get("y").asInt());
		             innerNode.put("width",value.get("width").asInt());
		             innerNode.put("height",value.get("height").asInt());
		             newSegmentsNode.add(innerNode);
			}
			return newNode;
		}
		
		private static ObjectNode parseIncorrectSegmentsTool(ObjectNode newNode, JsonNode annotations, JsonNode subjectNode,int toolNo) {
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode newSegmentsNode = newNode.putArray("incorrectSegments");
			for(JsonNode value:annotations.get("value"))
			    if (value.get("tool").asInt() == toolNo) {
			    	 ObjectNode innerNode = mapper.createObjectNode();
			         innerNode.put("x1",value.get("x1").asInt());
			         innerNode.put("x2",value.get("x2").asInt());
			         innerNode.put("y1",value.get("y1").asInt());
			         innerNode.put("y2",value.get("y2").asInt());

			         if (value.get("details").size()>0)
			           innerNode.put("segment", value.get("details").get(0).get("value").asInt());
			         if (subjectNode !=null) {
			        	int frame = value.get("frame").asInt()+1;	
			            innerNode.put("image", subjectNode.get("#img"+frame).asText());	
			         }
		             newSegmentsNode.add(innerNode);
			}		
			return newNode;
		}
		
		

		
		
		public static void parseWorkflowOne(String inputFilePath,String user,int month, int day) throws Exception {
			
			int pageNoTask=1;
		    int missingSegmentsTask = 0;
		    int missingSegTool= 0;

		 
		    File outputFile;
		    
		    String workflowName = "Workflow 1";
		   		    
		    Date date = new GregorianCalendar(2023, month-1, day).getTime();
		    
			ObjectMapper mapper = new ObjectMapper();
			
			FlatFileItemReader<ZooniAnnotatedData> itemReader = readZooniCSV(inputFilePath);
			
            ZooniAnnotatedData annotatedData = null;
			
			ArrayNode rootNode = mapper.createArrayNode();

			do {
				 annotatedData = itemReader.read();
				 
				 if (annotatedData != null && annotatedData.getWorkflow_name().startsWith(workflowName) 
						 && annotatedData.getUser_name().matches(user) && annotatedData.getCreated_at().after(date)) {
					 JsonNode annotationsNode = mapper.readTree(annotatedData.getAnnotations());
					 JsonNode subjectDataNode = mapper.readTree(annotatedData.getSubject_data());
					 JsonNode subjectNode = subjectDataNode.get(annotatedData.getSubject_ids());
					 ObjectNode newNode = mapper.createObjectNode();
					 newNode = parsePageNoTask(newNode,annotationsNode,subjectNode,pageNoTask);
					 newNode = parseMissingSegmentsTool(newNode,annotationsNode.get(missingSegmentsTask),missingSegTool);
					 rootNode.add(newNode);
				 }
			} while (annotatedData != null);
					

				outputFile = new File(Paths.get(inputFilePath).getParent()+"/wf1" +user.charAt(0)+".json");
			
			mapper.writeValue(outputFile, rootNode);

	
		}


		public static void parseWorkflowTwo(String inputFilePath,String user, int month, int day) throws Exception {
			
			int transcriptionTaskNo=0;
			int NewSegmentsTaskNo=1;
			int incorrectSegTool=0;

		    String workflowName = "Workflow 2";
		    
		    File outputFile;
		    
		    Date date = new GregorianCalendar(2023, month-1, day).getTime();
		    
			ObjectMapper mapper = new ObjectMapper();
			
			FlatFileItemReader<ZooniAnnotatedData> itemReader = readZooniCSV(inputFilePath);
			
            ZooniAnnotatedData annotatedData = null;
			
			ArrayNode rootNode = mapper.createArrayNode();
			
			do {
				 annotatedData = itemReader.read();
								 
				 if (annotatedData != null && annotatedData.getWorkflow_name().startsWith(workflowName) 
						 && annotatedData.getUser_name().matches(user) && annotatedData.getCreated_at().after(date)) {
					 JsonNode annotationsNode = mapper.readTree(annotatedData.getAnnotations());
					 JsonNode subjectDataNode = mapper.readTree(annotatedData.getSubject_data());
					 JsonNode subjectNode = subjectDataNode.get(annotatedData.getSubject_ids());
					 ObjectNode newNode = mapper.createObjectNode();
					 newNode.put("pageNo", subjectNode.get("pageNo").asText());
					 newNode = parseTranscriptions(newNode,annotationsNode.get(transcriptionTaskNo),subjectNode);
					 newNode = parseIncorrectSegmentsTool(newNode,annotationsNode.get(transcriptionTaskNo),subjectNode,incorrectSegTool);
					 newNode = parseNewSegmentsTask(newNode,annotationsNode.get(NewSegmentsTaskNo),subjectNode);
					 rootNode.add(newNode);
				 }

			} while (annotatedData != null);
					

				outputFile = new File(Paths.get(inputFilePath).getParent()+"/wf2" +user.charAt(0)+".json");
		
			
			mapper.writeValue(outputFile, rootNode);
			
		}
		

		private static ObjectNode parseNewSegmentsTask(ObjectNode newNode, JsonNode annotations, JsonNode subjectNode) {
			
			int newPlantTool=0;
			int newMarginTool=1;
			
			ObjectMapper mapper = new ObjectMapper();
			
			ArrayNode newSegmentsNode = newNode.putArray("newSegments");
			
			for(JsonNode value:annotations.get("value")) {
				 ObjectNode innerNode = mapper.createObjectNode();
				 if (value.get("tool").asInt() == newMarginTool) { 
					innerNode.put("segment", "margin");
				    innerNode.put("text", "@:" +value.get("details").get(0).get("value").asText());
	                innerNode.put("No", value.get("details").get(1).get("value").asText());
			     }		     
				 
				 else if (value.get("tool").asInt() == newPlantTool) { 
					innerNode.put("segment", "plant");
	                innerNode.put("No", value.get("details").get(0).get("value").asText());
			     }
	                innerNode.put("x",value.get("x").asInt());
	                innerNode.put("y",value.get("y").asInt());
	                innerNode.put("width",value.get("width").asInt());
	                innerNode.put("height",value.get("height").asInt());
				 
				newSegmentsNode.add(innerNode);
			}

			return newNode;
		}


		private static ObjectNode parseTranscriptions(ObjectNode newNode, JsonNode annotations, JsonNode subjectNode) {
			
			int outlineMarginTool=2;
			int incorrectPlantTool=0;
			
            ObjectMapper mapper = new ObjectMapper();
			
			ArrayNode newSegmentsNode = newNode.putArray("transcriptions");
			
			for(JsonNode value:annotations.get("value")) {
				 ObjectNode innerNode = mapper.createObjectNode();
				 int frame = value.get("frame").asInt()+1;	
				 if (value.get("tool").asInt() != incorrectPlantTool) {
				    innerNode.put("image", subjectNode.get("#img"+frame).asText());	
				    innerNode.put("text", "@:" +value.get("details").get(0).get("value").asText());
			        if (value.get("tool").asInt() == outlineMarginTool) {
		                innerNode.put("x",value.get("x").asInt());
		                innerNode.put("y",value.get("y").asInt());
		                innerNode.put("width",value.get("width").asInt());
		                innerNode.put("height",value.get("height").asInt());
		                innerNode.put("text", value.get("details").get(0).get("value").asText());
				    }
			     }		     
				newSegmentsNode.add(innerNode);
			}
			return newNode;

		}


		public static FlatFileItemReader<ZooniAnnotatedData> readZooniCSV(String inputFilePath) throws IOException {
			
		    FileSystemResource resource = new FileSystemResource(new File(inputFilePath));
			  
			Scanner scanner = new Scanner(resource.getInputStream());
			
			String header = scanner.nextLine();
			
			scanner.close();
	
			FlatFileItemReader<ZooniAnnotatedData> itemReader = new FlatFileItemReader<ZooniAnnotatedData>();
		
			itemReader.setResource(resource);
			
	        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");
			
			tokenizer.setNames(header.split(","));
			
			tokenizer.setStrict(false);
		
			SuffixRecordSeparatorPolicy policy = new SuffixRecordSeparatorPolicy();
			
			itemReader.setRecordSeparatorPolicy(policy);
			
			DefaultLineMapper<ZooniAnnotatedData> lineMapper = new DefaultLineMapper<ZooniAnnotatedData>();
					
			lineMapper.setFieldSetMapper(fields -> {
			    ZooniAnnotatedData annotatedData =  new ZooniAnnotatedData(fields.readString("workflow_name"),fields.readString("user_name"),fields.readString("annotations"),fields.readString("subject_data"),
			    		                        fields.readString("subject_ids"),fields.readDate("created_at")); 
			    return annotatedData;
			});

			lineMapper.setLineTokenizer(tokenizer);
			itemReader.setLineMapper(lineMapper);
			itemReader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());
			itemReader.setLinesToSkip(1);
			itemReader.open(new ExecutionContext());
			
			
			return itemReader;
		}



		

}
