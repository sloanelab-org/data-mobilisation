package org.sloanelab.IntegratingReviewersAnnotations.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;


public class ProduceImages {
	
	
	
	public static BufferedImage drawRectangle(BufferedImage image, Color color, int left, int top, int width, int height) throws IOException {
		BufferedImage copyOfImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = copyOfImage.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.setColor(color);
		g2d.setStroke(new BasicStroke(10));
		g2d.drawRect(left,top,width,height);
		g2d.dispose();
		return copyOfImage;
	}
	
	public static BufferedImage drawLine(BufferedImage image, Color color, int x1, int y1, int x2, int y2) throws IOException {
		BufferedImage copyOfImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = copyOfImage.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.setColor(color);
		g2d.setStroke(new BasicStroke(10));
		g2d.drawLine(x1, y1, x2, y2);
		g2d.dispose();
		return copyOfImage;
	}
	
	
	public static void produceImage (BufferedImage image,String path, float compression) throws IOException {
		
		ImageWriter writer = (ImageWriter) ImageIO.getImageWritersByFormatName("jpeg").next();
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(compression);
		writer.setOutput(new FileImageOutputStream(new File(path)));
		writer.write(null, new IIOImage(image, null, null), param);
		writer.dispose();
	}


	public static void produceImagesForWF2(String inputFolder, String file, String imagesFolder,String vol, int from, int to) throws IOException {
		
		BufferedImage markedImage = null;
		BufferedImage sourceImage = null;
		BufferedImage allMarkedImage = null;
		
		float compression = 0.2f;
		File[] images = new File(imagesFolder).listFiles();
		new File(inputFolder).mkdirs();
		
		ObjectMapper mapper = new ObjectMapper();
		
		List<Integer> pagesNumbers = null;
		
	
		ArrayNode nodes = (ArrayNode) mapper.readTree(new File(file));
		
		for (int i = from ; i <= to; i++ ) {
			
			if (pagesNumbers != null && ! pagesNumbers.contains(i)) continue;
			
			for(JsonNode node:nodes) {
				if (Integer.valueOf(node.get("pageNo").asText()) == i) {
					String pageNo = node.get("pageNo").asText();
					new File(inputFolder+pageNo).mkdirs();
					for (File image:images) 	
					  if (image.isFile() && image.getName().equals(node.get("imageFile").asText())) {
						  sourceImage = ImageIO.read(image);						  
						  if (image.length()>1600000) compression = 0.1f;
								else compression = 0.2f;
						  break;
				       }
					
					allMarkedImage = sourceImage;
					
					for (JsonNode plant:node.get("plants")) {
						 markedImage = drawRectangle(sourceImage,Color.red,plant.get("left").asInt(),plant.get("top").asInt(),
		                                             plant.get("width").asInt(),plant.get("height").asInt());
						 
						 allMarkedImage = drawRectangle(allMarkedImage,Color.red,plant.get("left").asInt(),plant.get("top").asInt(),
                                 plant.get("width").asInt(),plant.get("height").asInt());
						 
						 String fileName = plant.get("id").asText();
						 
						 for (JsonNode pair:node.get("pairs")) 
							  if (pair.get("plant").asText().equals(plant.get("id").asText())) 
								  for (JsonNode margin:node.get("margins")) 
									  if (pair.has("margin") && pair.get("margin").asText().equals(margin.get("id").asText())) {
										  markedImage = drawRectangle(markedImage,Color.red,margin.get("left").asInt(),margin.get("top").asInt(),
		                                             margin.get("width").asInt(),margin.get("height").asInt());
										  
										  allMarkedImage = drawRectangle(allMarkedImage,Color.red,margin.get("left").asInt(),margin.get("top").asInt(),
		                                             margin.get("width").asInt(),margin.get("height").asInt());
										  
										  fileName = fileName + "&margin";
									  }
									  						  
						 produceImage(markedImage,inputFolder+pageNo+"/"+fileName+ "-d" +".jpeg",getCompression(markedImage));   
						 }
					
					
					if (inputFolder.contains("WF3")) {
						if (node.has("header")) {
								markedImage = drawRectangle(sourceImage,Color.red,node.get("header").get("left").asInt(),node.get("header").get("top").asInt(),
										node.get("header").get("width").asInt(),node.get("header").get("height").asInt());
								produceImage(markedImage,inputFolder+pageNo+"/" + vol + "-" + pageNo+ "-header-d" +".jpeg",getCompression(markedImage));   
								allMarkedImage = drawRectangle(allMarkedImage,Color.red,node.get("header").get("left").asInt(),node.get("header").get("top").asInt(),								
										node.get("header").get("width").asInt(),node.get("header").get("height").asInt());
						}
						
						if (node.has("footer")) {
							markedImage = drawRectangle(sourceImage,Color.red,node.get("footer").get("left").asInt(),node.get("footer").get("top").asInt(),
									node.get("footer").get("width").asInt(),node.get("footer").get("height").asInt());
							produceImage(markedImage,inputFolder+pageNo+"/"+ vol + "-" + pageNo+ "-footer-d" +".jpeg",getCompression(markedImage));   
							allMarkedImage = drawRectangle(allMarkedImage,Color.red,node.get("footer").get("left").asInt(),node.get("footer").get("top").asInt(),								
									node.get("footer").get("width").asInt(),node.get("footer").get("height").asInt());
					    }

					}
					produceImage(allMarkedImage,inputFolder+pageNo+"/"+ vol + "-" + pageNo + "-d" +".jpeg",getCompression(allMarkedImage)); 
					}
			    }
			
		     }
	  }
	
	


	public static float getCompression (BufferedImage image) throws IOException {
		
		float compression = 0.2f;
		
		produceImage(image,"temp.jpeg",0.2f);
		
		File temp = new File("temp.jpeg");
		
		if (temp.length() >1000000) 
			compression = 0.1f;
		
		temp.delete();
		return compression;
		
	}
	

	
	

}
