package org.sloanelab.ParsinZooniverseFiles.controller;

import java.io.IOException;

import org.sloanelab.ParsingZooniverseFiles.service.CopyImages;
import org.sloanelab.ParsingZooniverseFiles.service.CreateZooniManifest;
import org.sloanelab.ParsingZooniverseFiles.service.ParseZooniAnnotations;
import org.sloanelab.ParsingZooniverseFiles.service.ProduceImages;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	

	
	@GetMapping("/imagesForWF1")
	public String imagesForWF1(@RequestParam("inputFolder") String inputFolder) throws Exception {
		CopyImages.getImagesforWF1Manifest(inputFolder);
		return "done";
	}
	

	@GetMapping("/createManifestForWF1")
	public String createManifestwf1(@RequestParam("folder") String imagesFolder) throws IOException {
		    return CreateZooniManifest.writeManifestwf1(imagesFolder);
	}
	

	@GetMapping("/parseZooniverseWF1")
	public String parseZooniVerfication(@RequestParam("file") String inputFile, @RequestParam("user") String user, 
			@RequestParam("month") int month, @RequestParam("day") int day) throws  Exception {
		ParseZooniAnnotations.parseWorkflowOne(inputFile,user,month,day);
		return "done";
	}
	
	@GetMapping("/imagesForWF2")
	public String produceImagesForWF2(@RequestParam("file") String file, @RequestParam("imagesFolder") String imagesFolder
			,@RequestParam("vol") String vol, @RequestParam("from") int from, @RequestParam("to") int to) throws Exception {
		ProduceImages.produceImagesForWF2("WF2",file,imagesFolder,vol,from,to);
		return "done";
	}
	

	@GetMapping("/createManifestForWF2")
	public void createManifestwf2(@RequestParam("folder") String inputFolder) {
		    try {
				CreateZooniManifest.writeManifestwf2(inputFolder,"WF2");
			} catch (Exception e) {e.printStackTrace();}
	}
	

	@GetMapping("/imagesForWF2Manifest")
	public String copySingleMarkedImages(@RequestParam("inputFolder") String inputFolder) throws Exception {
		CopyImages.getImagesforWF2Manifest(inputFolder,"WF2");
		return "done";
	}
		
	
	@GetMapping("/parseZooniverseWF2")
	public String parseZooniTranscription(@RequestParam("file") String inputFile, @RequestParam("user") String user,
			                              @RequestParam("month") int month, @RequestParam("day") int day) {
		try {
			ParseZooniAnnotations.parseWorkflowTwo(inputFile,user,month,day);
		} catch (Exception e) {e.printStackTrace();}
		return "done";
	}
	
	


}
