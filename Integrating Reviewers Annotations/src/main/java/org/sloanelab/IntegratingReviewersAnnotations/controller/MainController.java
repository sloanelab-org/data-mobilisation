package org.sloanelab.IntegratingReviewersAnnotations.controller;


import org.sloanelab.IntegratingReviewersAnnotations.service.IntegrateWF1;
import org.sloanelab.IntegratingReviewersAnnotations.service.IntegrateWF2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	

	
	@GetMapping("/integrateWF1")
	public String integrateWF1(@RequestParam("annotationsFile") String annotationsFilePath, @RequestParam("outputFile") String outputFilePath, @RequestParam("vol") String vol) throws Exception {
		
		IntegrateWF1.consolidateWF1(annotationsFilePath, outputFilePath, vol);
		
		return "done";
	}
	

	@GetMapping("/integrateWF12")
	public String integrateWF2(@RequestParam("annotationsFile") String annotationsFilePath, @RequestParam("outputFile") String outputFilePath, @RequestParam("vol") String vol) throws Exception {
		
		IntegrateWF2.consolidateWF2(annotationsFilePath, outputFilePath, vol);
		
		return "done";
	}


}
