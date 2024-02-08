package org.sloanelab.SplittingPlantNames.model;

import java.util.ArrayList;

public class SinglePlantName {
	
	String name,author,type;
	
	ArrayList<String> authors = new ArrayList<String>();
	

	public SinglePlantName () {}

	public SinglePlantName (String name, String author, String type) {
		this.name = name;
		this.author = author;
		this.type = type;
	}
	
 
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public ArrayList<String> getAuthors() {
		return authors;
	}

	public void setAuthors(ArrayList<String> authors) {
		this.authors = authors;
	}


}
