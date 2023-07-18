package org.sloanelab.dataMobilisation.service;

import java.io.File;
import java.awt.Color;
import java.io.FileWriter;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.awt.BasicStroke;
import javax.imageio.ImageIO;

import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

public class Utilities {

	public static String markedOutputPathFolder= "/MarkedImages/";
	public static String croppedOutputFolder= "/CroppedImages/";
	public static String processedOutputFolder= "/ProcessedImages/";
	public static Map<String,Integer> statistics = new HashMap<String,Integer>();
	
	public static void deleteImages (String path) {
		File[] images = new File(path).listFiles();
		if (images != null) 
			for (File image:images)
				image.delete();
	}
	
	public static void reduceImageSize (BufferedImage image,float compression,String path) throws IOException {
		
		ImageWriter writer = (ImageWriter) ImageIO.getImageWritersByFormatName("jpeg").next();
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(compression); 
		writer.setOutput(new FileImageOutputStream(new File(path)));
		writer.write(null, new IIOImage(image, null, null), param);
		writer.dispose();
	}
	
	
	public static BufferedImage cropImage(BufferedImage image, float left, float top, float width, float height, String fileName) throws IOException {
		   BufferedImage img = image.getSubimage(Math.round(image.getWidth() * left), Math.round(image.getHeight() *top),
				                                 Math.round(image.getWidth()* width), Math.round(image.getHeight()*height));
		   File outputfile = new File(fileName);
		   ImageIO.write(img, "jpg", outputfile);
		   return img;
		}
	
	public static float getCompression (BufferedImage image) throws IOException {
		
		float compression = 0.2f;
		
		Utilities.reduceImageSize(image,0.2f,"temp.jpeg");
		
		File temp = new File("temp.jpeg");
		
		if (temp.length() >1000000) 
			compression = 0.1f;
		
		temp.delete();
		return compression;
		
	}
	
	public static void initiateFolders (String folder) {
		new File(folder+processedOutputFolder).mkdirs();
		new File(folder+croppedOutputFolder).mkdirs();
		new File(folder+markedOutputPathFolder).mkdirs();
	}
	
	
	public static void initiateJsonFile (String filePath) throws IOException {
       FileWriter myWriter = new FileWriter(filePath);
       myWriter.write("[]");
       myWriter.close();
	}
	
	public static BufferedImage drawRectangle(BufferedImage image, float left, float top, float width, float height) throws IOException {
		
		BufferedImage copyOfImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = copyOfImage.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.setColor(Color.red);
		g2d.setStroke(new BasicStroke(10));
		g2d.drawRect(Math.round(image.getWidth()*left),Math.round(image.getHeight()*top), 
				     Math.round(image.getWidth()*width),Math.round(image.getHeight()*height));
		g2d.dispose();
		return copyOfImage;
	}
	
	
}
