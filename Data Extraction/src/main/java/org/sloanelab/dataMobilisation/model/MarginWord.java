package org.sloanelab.dataMobilisation.model;

import com.amazonaws.services.textract.model.Block;

public class MarginWord {
	Block word;
	String lineId;
	
	public MarginWord(Block word, String lineId) {
		this.word = word;
		this.lineId = lineId;
	}
	
	public Block getWord() {
		return word;
	}

	public String getLineId() {
		return lineId;
	}
}
