package org.sloanelab.dataMobilisation;

import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.util.ListIterator;
import org.sloanelab.dataMobilisation.service.Utilities;
import com.amazonaws.services.textract.model.Block;



public class ProcessOddPages extends ProcessPages {
		
	
	int imageNo;
	final String pageType= "Odd";
	final String footerPatterns="(§|&|[*]|a|ra).*";
	final String oddTitlePattern="Lib";
	final String rightMarginPatterns="(zocu|lo.|&|temp|vire).*";
	
	String process(List<Block> blocks, String documentPath) throws IOException {
						
		Utilities.deleteImages(outputPath);
		
		findPageBounding(blocks);
		
		image = ImageIO.read(new File(documentPath));
		
		lines = getLines(blocks);
		
		words = getWords(blocks);
	
		headers = extractHeaders(lines);
			
		rightMargin = findRightMargin(lines,words);
		
		pageNo = findPageNumber(lines);
		
		footerMargin = getFooterMargin(lines,words);
		
	    lines = extractRightMargins(lines,words);
	    
	    rightMargins = cleanRightMargins(rightMargins);
	    
	    marginParagraphs = extractIndentedMargins(rightMargins);
	    			   	   
		extractFooters(lines,words);
		
	    topSort(footers); 
 
		middleHandWritings = extractMiddleHandWritings(lines,words,5000,pageType);
		
		textParagraphs = extractIndentedText(lines,words);
		
		plants = getPlants(rightMargin,pageType);
		
		plantsToMargins = linkPlantsToMargin(plants,marginParagraphs);
		
		produceImages(pageType,documentPath);
		
		return "done";
		
	}
	
	   private List<Block> extractRightMargins (List<Block>lines, List<Block>words) {
		    ListIterator<Block> itr = lines.listIterator();
			while (itr.hasNext()) {
				Block line = itr.next();
				    if (leftOf(line) > rightMargin && ! textOf(line).toLowerCase().matches(rightMarginPatterns) && bottomOf(line)<footerMargin) {	
				    	List<Block> lineWords = findLineWords(line,words,5000,pageType);
				    	if (lineWords.size()>0)
					    	   rightMargins.add(lineWords);
						if (isAllHandWritings(line,words,5000,pageType))
				            itr.remove();
				   }
			}
			for (Block line:lines) {
				List<Block> marginWords = new ArrayList<>();
				if (leftOf(line) < rightMargin && bottomOf(line) < footerMargin) {
			        for (Block word:findLineWords(line,words,5000,pageType))
			    	     if (leftOf(word)> rightMargin) 
			    	    	 marginWords.add(word);
				}
				if (marginWords.size()>0)
				    rightMargins.add(marginWords);
			}			
			rightMargins = topSortMarginWords(rightMargins);
			return lines;
	   }
	   
	   
	   
	   

	  private List<List<Block>> cleanRightMargins(List<List<Block>> margins) {
	           Iterator<List<Block>> itr1 = rightMargins.iterator();
	           while (itr1.hasNext()) {
	        	   Iterator<Block> itr2 = itr1.next().iterator();
	        	   while (itr2.hasNext()) {
	        		   Block word = itr2.next();
	        	   if (textOf(word).toLowerCase().matches(rightMarginPatterns))
	        		   itr2.remove();
	              }
	           }
	        	   
			return margins;
		}
	

	@Override
	List<Block> extractHeaders(List<Block> lines) {
		Float top = null;
		List<Block> headers = new ArrayList<Block>();
		
        ListIterator<Block> itr = lines.listIterator();
        
		while (itr.hasNext()) {
			Block line = itr.next();
			if (line.getText().endsWith("R I AE")) {
				itr.previous();
				top = topOf(itr.previous());
                break;
		    }
			
			else if (line.getText().startsWith(oddTitlePattern)) {
				top = topOf(line);
				break;
			}
		}

		
		ListIterator<Block> itr2 = lines.listIterator();
		
		while (itr2.hasNext()) {
			 Block line =  itr2.next();
			if (topOf(line) + 0.005 < top) {
				headers.add(line);
				itr2.remove();
			}
		}
		
		return headers;
	}

	@Override
	String findPageNumber(List<Block> lines) {
		ListIterator<Block> itr = lines.listIterator();
		
		while (itr.hasNext()) {
			Block blk = itr.next();
			
			if (blk.getText().endsWith("R I AE")) {
				itr.previous();
				blk = itr.previous();
				headerMargin = topOf(blk) - 0.01f;
				itr.next();itr.next();
				leftMargin = leftOf(itr.next()) + 0.005f;
				itr.remove();
				updateMargins(blk);
				return blk.getText();
			}
			
			if (blk.getText().startsWith("De Herbis") || blk.getText().startsWith("De Plantis")) {
				blk = itr.next();
				itr.remove();
				updateMargins(blk);
				return blk.getText();
			}
		}
		
		return "E" + ++errorPageNo;
	}

	private void updateMargins(Block blk) {
		
		if (rightMargin == 0)
			rightMargin = leftOf(blk) - 0.013f;
		
		if (pageRight - rightMargin > 0.18) {
		    pageRight = rightMargin + 0.2f;
		    pageWidth = pageRight - pageLeft;
	   }
	
	}

	@Override
	float getFooterMargin(List<Block> lines, List<Block> words) {
	    ListIterator<Block> itr = lines.listIterator(lines.size());
		int c = 0;
		float margin = pageBottom;
		while (itr.hasPrevious()) {
			Block line = itr.previous();
			List<Block> lineWords = findLineWords(line,words,5000,pageType);
			if(lineWords.size()>1)
			   for (Block word:lineWords) 
				  if (!word.getTextType().equals("HANDWRITING") && ++c>1)
					   return margin;
		    if ((lineWords.size()== 1 && ! lineWords.get(0).getTextType().equals("HANDWRITING")) || leftOf(line)>this.rightMargin )
		    	   continue;
			margin = topOf(line);
		}
		return margin;
	}
	
	@Override
	List<List<List<Block>>> extractIndentedMargins(List<List<Block>> margins) {
		Float bottom = 0f;
		List<List<Block>> marginParagraph = new ArrayList<>();
		List<List<List<Block>>> marginParagraphs = new ArrayList<>();
		
		for (List<Block> line:margins) {
			Block word = line.stream().filter(str -> !str.getText().matches("-|-|--")).findFirst().orElse(null);
			if (word == null) continue;
			float wordTop = topOf(word);
			if (wordTop > this.footerMargin) break; 
			if(wordTop - bottom > 0.006) { 
				marginParagraphs.add(marginParagraph);
				marginParagraph = new ArrayList<>();
			    marginParagraph.add(line);			    
			}
			else 
				marginParagraph.add(line);
			
		    bottom = bottomOf(word);		
		}
		
		marginParagraphs.add(marginParagraph);
	
		return marginParagraphs;
		
		}
	

	@Override
	void extractFooters(List<Block> lines, List<Block> words) {
	    ListIterator<Block> itr = lines.listIterator(lines.size());
		int c = 0;
		while (itr.hasPrevious()) {
			Block line = itr.previous();
			if(leftOf(line) > image.getWidth() * 0.8 && textOf(line).toLowerCase().matches(footerPatterns)) continue;
			List<Block> lineWords = findLineWords(line,words,5000,pageType);
			if(lineWords.size()>1)
			   for (Block word:lineWords) 
				  if (!word.getTextType().equals("HANDWRITING") && ++c>1)
					   return;
		    if (lineWords.size()== 1 && !lineWords.get(0).getTextType().equals("HANDWRITING"))
		    	   continue;
			this.footers.add(line);
			itr.remove();
		}
	}


	@Override
	List<List<Block>> extractIndentedText(List<Block> lines, List<Block> words) {
		
		Float top = 0f;
		Float left = 0f;
		List<Block> paragraph = new ArrayList<>();
		List<List<Block>> paragraphs = new ArrayList<>();
		
		
		if (leftMargin !=0 && headerMargin !=0) {
			top = headerMargin;
			left = leftMargin;
		}
		
		else {
		
		   for (Block line:lines) {
			   if (line.getText().startsWith("Lib.")) {
				  leftMargin = leftOf(line);
				  headerMargin = topOf(line);
				  top = bottomOf(line);
				  left = leftOf(line);
			  }
		  }
		}
	
		
		for (Block line:lines) {
			List<Block> lineWords = findLineWords(line,words,this.rightMargin,pageType);
			if (lineWords.size()==1 && heightOf(lineWords.get(0)) >0.02 ) continue;
			if(!lineWords.isEmpty() && isIndented(top,left,lineWords) &&
					!(paragraph.size()>0 && paragraph.get(paragraph.size()-1).getText().matches("A[.]\\s*|[*]\\s*"))) { 
				paragraphs.add(paragraph);
				paragraph = new ArrayList<>();
			    paragraph.add(line);			    
			    top = findTopLineWords(lineWords, top);
			    left = findLeftLineWords(lineWords,left);
			    
			}
			else {
				paragraph.add(line);
				if (!lineWords.isEmpty()) {
				  top = findTopLineWords(lineWords,top);
				  left = findLeftLineWords(lineWords,left);
				}
			}
		}
		
		paragraphs.add(paragraph);
	
		return paragraphs;
	}


	@Override
	float findRightMargin(List<Block> lines, List<Block> words) {
		for (Block line: lines) 
		    if (line.getText().matches("Locus.?.?"))
		    	  return leftOf(line) - 0.005f;	
		
		for (Block word:words)
			if (word.getText().matches("Locus.?.?"))
				  return leftOf(word) - 0.005f;
	
		return 0;
	}


	@Override
	List<Block> sortParagraphWords(List<Block> paragraphWords) {
		float top = 0f;
		
		paragraphWords = leftSort(paragraphWords);
		
		List<Block> temp = new ArrayList<>();
		
		List<Block> sortedWords = new ArrayList<>();
		
		for (Block word:paragraphWords)
		    if (sortedWords.isEmpty()  || topOf(word) - top < 0.005) {
	    	     sortedWords.add(word);
	    	     top = topOf(word);
	    }
	    else temp.add(word);
		
		sortedWords.addAll(temp);
		
		return sortedWords;
	}
	
}
