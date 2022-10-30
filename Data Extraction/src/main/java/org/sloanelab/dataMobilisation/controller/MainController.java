package org.sloanelab.dataMobilisation.controller;


import java.io.File;
import java.io.IOException;
import org.sloanelab.dataMobilisation.ProcessPages;
import org.sloanelab.dataMobilisation.service.Utilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	
	
	
	@Value("${cloud.aws.region}")
	String region;
	@Value("${cloud.aws.credentials.accessKey}")
    String access;
	@Value("${cloud.aws.credentials.secretKey}")
	String secret;

	@GetMapping("/processImage")
	public String processImage(@RequestParam("image") String imagePath) throws IOException {
		    return ProcessPages.process(imagePath,region,access,secret);
	}
	
	@GetMapping("/processImagesFolder")
	public String processImagesFolder(@RequestParam("folder") String inputFolder) throws IOException {
		Utilities.initiateFolders(inputFolder);
		ProcessPages.errorPageNo = 0;
		File[] files = new File(inputFolder).listFiles();
	    for (File file:files) {
	    	if (file.getName().endsWith(".jpg")) {
	    	  try {	
		        String outcome = ProcessPages.process(file.getPath(),region,access,secret);
		        if (outcome.equals("done"))
	    	       file.renameTo(new File(inputFolder+Utilities.processedOutputFolder+file.getName()));
	    	  }
	    	  catch (Exception e) {
	    		  System.out.println("******************************************************");
	    		  System.out.println("error in:" + file.getName());
	    		  e.printStackTrace();
	    	  }
	    	}
	    }
	    return "done";
	}
	
}
