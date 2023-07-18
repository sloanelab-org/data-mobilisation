package org.sloanelab.dataMobilisation.model;

import java.util.List;
import java.util.ArrayList;

import com.amazonaws.services.textract.model.Block;

public class LinkedPlantsToMargins {
	
	List<Block> plantWords = new ArrayList<>();
	List<List<Block>> margins = new ArrayList<>();
	
	
	public List<Block> getPlantWords() {
		return plantWords;
	}
	public void setPlantWords(List<Block> plantWords) {
		this.plantWords = plantWords;
	}

	public List<List<Block>> getMargins() {
		return margins;
	}
	
	public void setMargins(List<List<Block>> margins) {
		this.margins = margins;
	}
	

	


}
