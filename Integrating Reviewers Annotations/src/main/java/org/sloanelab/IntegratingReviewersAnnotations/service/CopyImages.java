package org.sloanelab.IntegratingReviewersAnnotations.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
public class CopyImages {
	
	
	public static void getImagesforWF1Manifest(String inputFolder) throws IOException {
		
		
		new File(inputFolder+"allMarked").mkdirs();
		
		File[] inputFoolders = new File(inputFolder).listFiles();
		
		for(File folder:inputFoolders ) {

			File[] images = folder.listFiles();
			if (images == null) continue;
			
			Arrays.sort(images, Comparator.comparingLong(File::lastModified));
			
			for (File image:images) {	
			  if (!image.isFile() || image.getName().startsWith(".")) continue;
			  if (image.getName().split("-").length ==3) {
				  Files.copy(image.toPath(), Paths.get(inputFolder+"allMarked/"+image.getName()),StandardCopyOption.REPLACE_EXISTING);
			  }
			}	
		}
	}
	
	
	public static void getImagesforWF2Manifest(String inputFolder, String workflow) throws IOException {
		
		new File(inputFolder+"/singleMarked").mkdirs();
		
		File[] inputFoolders = new File(inputFolder).listFiles();
		
		for(File folder:inputFoolders ) {

			if (! Character.isDigit(folder.getName().charAt(0))) continue;
			
			File[] images = folder.listFiles();
			if (images == null) continue;
			
			Arrays.sort(images, Comparator.comparingLong(File::lastModified));
			
			for (File image:images) {	
			  if (!image.isFile() || image.getName().startsWith(".")) continue;
			  if(workflow.equals("WF2") && (image.getName().contains("header") || image.getName().contains("footer"))) continue;
			  Files.copy(image.toPath(), Paths.get(inputFolder+"/singleMarked/"+image.getName()),StandardCopyOption.REPLACE_EXISTING);
			 
			}	
		}
			
	}

}
