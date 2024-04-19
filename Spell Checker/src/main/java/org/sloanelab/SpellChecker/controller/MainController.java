package org.sloanelab.SpellChecker.controller;


import java.io.File;

import org.sloanelab.SpellChecker.service.SpellCheckingPlantNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	
	@Autowired
	SpellCheckingPlantNames spellCheckingPlantNames;
	

		
	@GetMapping("/spellCheck")
	public void spellCheck(@RequestParam("file") String inputFile, @RequestParam("from") int from) {
		try {
			spellCheckingPlantNames.spellCheckPlantNameFile(inputFile,new File(inputFile).getParent()+ "/SpellCheckedResults.json",from);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}


}
