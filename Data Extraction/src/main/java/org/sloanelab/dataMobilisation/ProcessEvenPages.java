package org.sloanelab.dataMobilisation;

import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ListIterator;
import org.sloanelab.dataMobilisation.service.Utilities;
import com.amazonaws.services.textract.model.Block;



public class ProcessEvenPages extends ProcessPages {
	
	int imageNo;
	final float evenPageLeftMargin = 0.22f;
	final String pageType= "Even";
	final String footerPattern="(§|&|[*]).*";

	String process(List<Block> blocks, String documentPath) throws IOException {
		
		Utilities.deleteImages(outputPath);
		
        findPageBounding(blocks);
        
        image = ImageIO.read(new File(documentPath));
        
		lines = getLines(blocks);
		
		words = getWords(blocks);
		
		headers = extractHeaders(lines);
				
		pageNo = findPageNumber(lines);
	
		footerMargin = getFooterMargin(lines,words);
				   
	    rightMargin = findRightMargin(lines,words);
				
		lines = extractLeftMargins(lines,words);
		leftMargins = cleanLeftMargins(leftMargins);

	
		marginParagraphs = extractIndentedMargins(leftMargins);

		extractFooters(lines,words);
	    topSort(footers);
	    

		middleHandWritings = extractMiddleHandWritings(lines,words,evenPageLeftMargin,pageType);

		textParagraphs = extractIndentedText(lines,words);
		
		plants = getPlants(leftMargin,pageType);
				
		plantsToMargins = linkPlantsToMargin(plants,marginParagraphs);
		
		produceImages(pageType,documentPath);
		
		return "done";
		
	}
	
	
	@Override
	List<Block> extractHeaders(List<Block> lines) {
		Float top = null;
		List<Block> headers = new ArrayList<Block>();
		
		
		ListIterator<Block> lineItr = lines.listIterator();
		while (lineItr.hasNext()) {
			Block line = lineItr.next();
			if (line.getText().endsWith("R I AE")) {
				lineItr.previous();
				top = topOf(lineItr.previous());
                break;
		    }
			
			else if (line.getText().toLowerCase().matches(titlePattern)) {
				top = topOf(line);
				break;
			}
		}
		
	
		ListIterator<Block> itr = lines.listIterator();
		
		while (itr.hasNext()) {
			 Block line =  itr.next();
			if (topOf(line) + 0.005 < top) {
				headers.add(line);
				itr.remove();
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
				itr.remove();
				this.leftMargin = blk.getGeometry().getPolygon().get(2).getX();
				this.headerMargin = topOf(blk) + 0.01f;
				updateMargins();
				return blk.getText();
			}
			
			else if (blk.getText().toLowerCase().matches(titlePattern)) {
			    itr.previous();
				blk = itr.previous();
				itr.remove();
				this.leftMargin = blk.getGeometry().getPolygon().get(2).getX();
				updateMargins();
				return  blk.getText();
			}
		}
		
		return "E"+ ++errorPageNo;
	}

	@Override
	float getFooterMargin(List<Block> lines, List<Block> words) {
	    ListIterator<Block> itr = lines.listIterator(lines.size());
		int c = 0;
		float margin = pageBottom;
		while (itr.hasPrevious()) {
			Block line = itr.previous();
			List<Block> lineWords = findLineWords(line,words,0,pageType);
			if(lineWords.size()>1)
			   for (Block word:lineWords) 
				  if (!word.getTextType().equals("HANDWRITING") && ++c>1)
					   return margin;
		    if (lineWords.size()== 1 && !lineWords.get(0).getTextType().equals("HANDWRITING"))
		    	   continue;
			margin = topOf(line);
		}
		return margin;
	}

	
	private List<Block> extractLeftMargins (List<Block>lines, List<Block>words) {
		   
	    ListIterator<Block> itr = lines.listIterator();
	    

		while (itr.hasNext()) {
			Block line = itr.next();
			    if (rightOf(line) < leftMargin && ! textOf(line).toLowerCase().matches(marginPatterns) && bottomOf(line) <footerMargin) {
			    	List<Block> lineWords = findLineWords(line,words,evenPageLeftMargin,pageType);
			    	if (lineWords.size()>0)
			    	   leftMargins.add(lineWords);
					if (isAllHandWritings(line,words,evenPageLeftMargin,pageType))
			            itr.remove();
			   }
		}
		
		for (Block line:lines) {
			List<Block> marginWords = new ArrayList<>();
			if (rightOf(line) > leftMargin && bottomOf(line) < footerMargin) {
		        for (Block word:findLineWords(line,words,evenPageLeftMargin,pageType))
		    	     if (leftOf(word)< leftMargin - 0.02) 
		    	    	 marginWords.add(word);
			}
			
			if (marginWords.size()>0)
			    leftMargins.add(marginWords);
		}
		
		
		leftMargins = topSortMarginWords(leftMargins);
		return lines;
   }

	
	private List<List<Block>> cleanLeftMargins(List<List<Block>> margins) {
        Iterator<List<Block>> itr1 = margins.iterator();
        while (itr1.hasNext()) {
     	   Iterator<Block> itr2 = itr1.next().iterator();
     	   while (itr2.hasNext()) {
     		   Block word = itr2.next();
     	   if (word.getText().toLowerCase().matches(marginPatterns))
     		   itr2.remove();
     	   }
        }
		return margins;
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
			if (leftOf(line) > image.getWidth() * 0.8 && textOf(line).toLowerCase().matches(footerPattern)) continue;
			List<Block> lineWords = findLineWords(line,words,evenPageLeftMargin,pageType);
			if(lineWords.size()>1)
			   for (Block word:lineWords) 
				  if (!word.getTextType().equals("HANDWRITING") && ++c>1)
					   return;
		    if (lineWords.size()== 1 && !lineWords.get(0).getTextType().equals("HANDWRITING"))
		    	   continue;
			this.footers.add(line);
			itr.remove();
		}
		return;
		
	}

	@Override
	List<List<Block>> extractIndentedText(List<Block> lines, List<Block> words) {
		Float top = 0f;
		Float left = 0f;
		List<Block> paragraph = new ArrayList<>();
		List<List<Block>> paragraphs = new ArrayList<>();
		

		if (headerMargin != 0 && leftMargin !=0) {
			top = headerMargin;
			left = leftMargin;
		}
		
		else {
		
		  for (Block line:lines) {
			  if (line.getText().toLowerCase().matches(titlePattern)) {
			    	this.headerMargin = topOf(line);
				    top = bottomOf(line);
				    left = leftOf(line);
			   }
		   }
		}
		
		

		for (Block line:lines) {
			List<Block> lineWords = findLineWords(line,words,this.leftMargin,pageType);
			if (lineWords.size()==1 && heightOf(lineWords.get(0)) >0.01 ) continue;
			if(!lineWords.isEmpty() && isIndented(top,left,lineWords) &&
					!(paragraph.size()>0 && paragraph.get(paragraph.size()-1).getText().matches("A[.]\\s*|[*]\\s*"))) { 
				paragraphs.add(paragraph);
				paragraph = new ArrayList<>();
			    paragraph.add(line);	
			    top = findTopLineWords(lineWords,top);
			    left = findLeftLineWords(lineWords,left);
			    
			}
			else {
				paragraph.add(line);
				if (!lineWords.isEmpty()) {
				  top= findTopLineWords(lineWords,top);
				  left = findLeftLineWords(lineWords,left);
				}
			}
		}
		
		paragraphs.add(paragraph);
	
		return paragraphs;
	}
	
	
	 void updateMargins() {
		  if (leftMargin - pageLeft > 0.18) {
		    	  float orginalPageLeft = pageLeft;
			      pageLeft = leftMargin - 0.2f;
			      pageWidth = pageWidth - (orginalPageLeft - pageLeft);
		   }
		
	}

	@Override
	List<Block> sortParagraphWords(List<Block> paragraphWords) {
		float top = 0f;
		
		ListIterator<Block> itr = paragraphWords.listIterator();
		
		List<Block> temp = new ArrayList<>();
		
		List<Block> sortedWords = new ArrayList<>();
		
		while (itr.hasNext()) {

		    Block word = itr.next();	
		   		    		    
		    if (sortedWords.isEmpty()  || topOf(word) - top < 0.005) {
		    	     sortedWords.add(word);
		    	     top = topOf(word);
		    }
		    else temp.add(word);
		      
		}
		sortedWords.addAll(temp);
		return sortedWords;
	}

	@Override
	float findRightMargin(List<Block> lines, List<Block> words) {
		for (Block line:lines)
			if (line.getGeometry().getPolygon().get(2).getX() > super.rightMargin)
				super.rightMargin = line.getGeometry().getPolygon().get(2).getX();
	    return super.rightMargin;
	}
	
	
	
}
