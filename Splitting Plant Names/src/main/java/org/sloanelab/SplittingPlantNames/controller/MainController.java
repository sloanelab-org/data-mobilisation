package org.sloanelab.SplittingPlantNames.controller;


import org.sloanelab.SplittingPlantNames.service.PlantNameSplitting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	
	@Autowired
	PlantNameSplitting plantSplitting;
	
	@GetMapping("/splitPlantNames")
	public void split (@RequestParam ("file") String file) {
		
		try {
			plantSplitting.splitAllPlantNames(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
