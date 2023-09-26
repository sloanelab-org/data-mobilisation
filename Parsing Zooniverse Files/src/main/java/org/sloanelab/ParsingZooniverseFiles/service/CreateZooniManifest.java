package org.sloanelab.ParsingZooniverseFiles.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import com.opencsv.CSVWriter;

public class CreateZooniManifest {
	

	public static String writeManifestwf1(String inputPath) throws IOException  {
		
		FileWriter outputfile = new FileWriter(new File(inputPath+"/manifest.csv"));
		CSVWriter writer = new CSVWriter(outputfile);
		String[] header = {"pageNo", "#id", "#image"};
		writer.writeNext(header);
		
		File[] foolders = new File(inputPath).listFiles();
		
		for(File folder:foolders ) {

			File[] images = folder.listFiles();
			if (images == null) continue;
			
			Arrays.sort(images, Comparator.comparingLong(File::lastModified));
			
			String[] data = new String[3];
			
			data[0] = data[1] =  folder.getName();
			
			for (File image:images) {	
			  if (!image.isFile() || image.getName().startsWith(".")) continue;
			  if (image.getName().split("-").length ==3)
			    	data[2] = image.getName();  
			}	
			writer.writeNext(data);
		}
	    writer.close();
	    return "done";
	}
	
	public static String writeManifestwf2(String inputFolder, String workflow) throws IOException  {
		
		FileWriter outputfile = new FileWriter(new File(inputFolder+"manifest.csv"));
		CSVWriter writer = new CSVWriter(outputfile);
		String[] header = {"pageNo","#img1","#img2","#img3","#img4","#img5","#img6","#img7","#img8","#img9","#img10"
				           ,"#img11","#img12","#img13","#img14","#img15","#img16","#img17","#img18","#img19","#img20","#img21","#img22","#img23","#img24"};
		writer.writeNext(header);
		
		File[] foolders = new File(inputFolder).listFiles();
		
		for(File folder:foolders ) {
			
			if (! Character.isDigit(folder.getName().charAt(0))) continue;
						
			File[] images = folder.listFiles();
			
			if (images == null) continue;
			
			Arrays.sort(images, Comparator.comparingLong(File::lastModified));
			
			String[] data = new String[25];
			
			data[0] = folder.getName();
			int counter = 1;
			
			for (File image:images) {
			  if (!image.isFile() || image.getName().startsWith(".")) continue;
			  
			  if (image.getName().split("-").length >2)
				  if (workflow.equals("WF2") && (image.getName().contains("header") || image.getName().contains("footer")))
					  continue;
				  else
				    data[counter++] = image.getName();
			  		
			}

			  counter--;
			  String[] data2 = new String[25];	  
			  data2[0] = data[0];
			  data2[1] = data[counter];		
			  for (int i = 2; i <= counter; i++) 
				 data2[i] = data[i-1];
						
			writer.writeNext(data2);
		}
	    writer.close();
	    return "done";
	}

}
