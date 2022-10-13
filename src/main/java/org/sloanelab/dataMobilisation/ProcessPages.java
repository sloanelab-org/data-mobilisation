package org.sloanelab.dataMobilisation;

import java.io.File;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.util.ListIterator;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import com.amazonaws.util.IOUtils;
import java.awt.image.BufferedImage;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import org.sloanelab.dataMobilisation.service.Utilities;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.textract.model.Relationship;
import org.sloanelab.dataMobilisation.model.LinkedPlantsToMargins;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.AnalyzeDocumentRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest; 


public abstract class ProcessPages {
	
	String pageNo;
	public static int errorPageNo = 0;
	float leftMargin = 0f;
	float rightMargin = 0f;
	float footerMargin = 0;
	float headerMargin = 0f;
	BufferedImage image = null;
	static float compression;
	static String outputPath = "Plants/";
	List<Block> words = new ArrayList<>();
	List<Block> lines = new ArrayList<>();
	List<Block> footers = new ArrayList<>();
	List<Block> headers = new ArrayList<>();
	List<List<Block>> plants = new ArrayList<>();
	List<List<Block>> leftMargins = new ArrayList<>();
	List<List<Block>> rightMargins = new ArrayList<>();
	List<Block> middleHandWritings = new ArrayList<>();
	List<List<Block>> textParagraphs = new ArrayList<>();
	List<List<List<Block>>> marginParagraphs = new ArrayList<>();
	List<LinkedPlantsToMargins> plantsToMargins = new ArrayList<>();
	float pageTop, pageLeft, pageRight, pageWidth, pageBottom, pageHieght;
    List<String> filteringPatterns = Arrays.asList("Lo.us.{1,2}","Vire.{1,3}","Temp.{1,4}","Zo.us.{1,4}");
	
    final String volNo = "I";
	final String plantPatterns=".+(c[.] ?b[.]|c[.] ?b:|j[.] ?b[.]|j[.] ?b:).*";
	final String marginPatterns="(zocu|loc|&|temp|vire).*";
	final String antiPlantPatterns="(c[.]|f[.]|j[.]|7[.]|3[.]) ?b.*";
	final String handwrittenStarterPatterns=".*(hs|js|a[.]s[.]).*";
	
	final static String titlePattern="(h 1 s t o|histor|h i s t o).*";
	
	abstract String findPageNumber(List<Block> lines);
	abstract List<Block> extractHeaders(List<Block> lines);
	abstract void extractFooters(List<Block> lines,List<Block>words);
	abstract float getFooterMargin(List<Block> lines,List<Block>words);
	abstract List<Block> sortParagraphWords (List<Block> paragraphWords);
	abstract float findRightMargin(List<Block> lines, List<Block> words);
	abstract List<List<List<Block>>> extractIndentedMargins(List<List<Block>> margins);
	abstract List<List<Block>> extractIndentedText(List<Block> lines, List<Block>words);
	
		
    public static String process(String imagePath, String region, String access, String secret) throws IOException{
    	
		ByteBuffer imageBytes=null;
		
		File file = new File(imagePath);
		
		if (file.length()>1600000) compression = 0.1f;
		else compression = 0.2f;
		
		try (InputStream inputStream = new FileInputStream(new File(imagePath))) {
		    imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
		}
		catch (Exception e) {e.printStackTrace();}
		
		AWSCredentialsProvider awsCreds = new AWSStaticCredentialsProvider(new BasicAWSCredentials(access,secret));
		AmazonTextract client = AmazonTextractClientBuilder.standard().withRegion(region).withCredentials(awsCreds).build();
				
		Object result = analyzeDocument(imageBytes,client);
		
		List<Block> blocks = null;
		
		if (result instanceof AnalyzeDocumentResult)
		      blocks = ((AnalyzeDocumentResult) result).getBlocks();
		else if(result instanceof DetectDocumentTextResult)
			blocks = ((DetectDocumentTextResult) result).getBlocks();
		
		ListIterator<Block> itr = blocks.listIterator();
		while (itr.hasNext()) {
			Block block = itr.next();
			if (block.getBlockType().equals("LINE") && block.getText().endsWith("R I AE")) {
				itr.previous();
				String page = itr.previous().getText();
				if (Integer.valueOf(page) % 2 == 0)
					return new ProcessEvenPages().process(blocks, imagePath);
				else if (Integer.valueOf(page) % 2 == 1)
					return new ProcessOddPages().process(blocks,imagePath);
			}
			else if (block.getBlockType().equals("LINE") && block.getText().startsWith("Lib."))
				return new ProcessOddPages().process(blocks,imagePath); 
			else if (block.getBlockType().equals("LINE") && block.getText().toLowerCase().matches(titlePattern))
				return new ProcessEvenPages().process(blocks, imagePath);
		}
		
		return "fail";
		
    }
    


    protected static Object detectDocument (ByteBuffer imageBytes,AmazonTextract client) {
		DetectDocumentTextRequest request = new DetectDocumentTextRequest()
        .withDocument(new Document()
                .withBytes(imageBytes));
        DetectDocumentTextResult result = client.detectDocumentText(request);	
        return result;
    }
    
    
    protected static Object analyzeDocument (ByteBuffer imageBytes,AmazonTextract client) {
		AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
				   .withFeatureTypes("TABLES")
				   .withDocument(new Document()
						   .withBytes(imageBytes));
		AnalyzeDocumentResult result = client.analyzeDocument(request);	
		return result;
    }
    
	
	
	protected void findPageBounding(List<Block> blocks) {
        for (Block blk:blocks)
      	      if (blk.getBlockType().equals("PAGE")) {
      	    	       pageTop = topOf(blk);
      	    	       pageLeft = leftOf(blk);
      	    	       pageWidth = widthOf(blk);
      	    	       pageHieght = heightOf(blk);
      	    	       pageBottom = bottomOf(blk);
      	    	       pageRight = pageLeft + pageWidth;
      	    	       break;
      	      }
	}
	
	protected List<Block> getLines(List<Block> blocks) {
		List<Block> lines = new ArrayList<>();
		for (Block blk: blocks)
			if (blk.getBlockType().equals("LINE"))
				lines.add(blk);
		return lines;
	}

	protected List<Block> getWords(List<Block> blocks) {
		List<Block> words = new ArrayList<>();
		for (Block blk: blocks)
			if (blk.getBlockType().equals("WORD"))
				words.add(blk);
		return words;
	}

	
	protected boolean isAllHandWritings(Block blk,List<Block>words,float margin,String type) {
		List<Block> lineWords = findLineWords(blk,words,margin,type);
		for (Block word:lineWords)
			if (!word.getTextType().equals("HANDWRITING"))
				return false;
		return true;
	}
	
	protected List<Block> findLineWords (Block line, List<Block>words, float margin, String type) {
	    List<Block> lineWords = new ArrayList<>();	
	    Relationship relation = line.getRelationships().get(0);
	    List<String> childIds;
	    if (relation.getType().equals("CHILD")) {
		   childIds = relation.getIds();
		   for (String id:childIds)
			   for (Block word:words)
				   if (word.getId().equals(id) && type.equals("Odd") && rightOf(word) < margin)
					  lineWords.add(word);
				   else if (word.getId().equals(id) && type.equals("Even") && leftOf(word) > margin)
						  lineWords.add(word);
	    }
	    
	    lineWords = leftSort(lineWords);
	    
		return lineWords;
	}
	
	
	List<Block> findPlant(List<Block> paragraph, List<Block> words, float margin, String pageType) throws IOException {
		int c = 1;
		float top = 0;
		String fstWord = "";
		List<Block> paragraphWords = new ArrayList<>();
		
		Block fstLine = paragraph.get(0);
		
		if (textOf(fstLine).length()> 5 && textOf(fstLine).toUpperCase().equals(textOf(fstLine)))
			return paragraphWords;
		
		
		if(textOf(fstLine).toLowerCase().matches(antiPlantPatterns) 
				|| textOf(fstLine).toLowerCase().matches(titlePattern)
				) 
			
			return paragraphWords;
			
		List<Block> tempWords = findLineWords(fstLine,words,margin,pageType);
	
		tempWords = stripHandWritings(tempWords,pageType);
		
		if (tempWords.size()> 0)
			fstWord = tempWords.get(0).getText();
	
		if(!fstWord.matches("[*]|[6|$|§|&][.].*|..?[6|.|:]") && ! textOf(fstLine).toLowerCase().matches(plantPatterns))	
			return paragraphWords;
		
		
		
		for (Block line:paragraph) {

			
			List<Block> lineWords = stripHandWritings(findLineWords(line,words,margin,pageType),pageType);
			
			if (lineWords.size()>0 &&  heightOf(lineWords.get(0))>0.03)
				return paragraphWords;
			
			if (lineWords.size()==0 ||  (lineWords.size() == 1 && lineWords.get(0).getText().length()<3))
				continue;
			
		}
		


		for (Block line:paragraph) {
			if (top == 0 || topOf(line) < top)
				top = topOf(line);
			paragraphWords.addAll(stripHandWritings(findLineWords(line,words,margin,pageType),pageType));
		}
				
		paragraphWords = sortParagraphWords(paragraphWords);
	
		return paragraphWords;
	}
	
	
	protected Boolean isIndented(Float top, Float left, List<Block>lineWords) {	
		
        if (topOf(lineWords.get(0)) < top)
        	return false;  
        
		for (Block word:lineWords) 
			if (topOf(word) - top > 0.014 ) 
				return true;
			else if (word.getTextType().equals("HANDWRITING") && ! textOf(word).matches("6[.].*|..?[.]"))
				continue;
			else return false;
		return false;
	}
	
	protected List<List<Block>> getPlants(float margin,String pageType) throws IOException {
	     for (List<Block> paragraph: textParagraphs) {
		     if (paragraph.isEmpty()) continue;
	         List<Block> plantWords = findPlant(paragraph,words,margin,pageType);
	         if (plantWords.isEmpty()) continue;
	         if (plantWords.isEmpty() || leftOf(plantWords.get(0)) > 0.9) continue;
             plants.add(plantWords);
	     }
	     return plants;
	}
	
	protected float findTopPlant (List<Block>plant) {
		Float top = topOf(plant.get(0));	
		for (Block word:plant)
		     if (topOf(word)<top)
		    	 top = topOf(word);
		return top;
	} 
	
	protected float findBottomPlant (List<Block>plant) {
		Float bottom = bottomOf(plant.get(0));	
		for (Block word:plant)
		     if (bottomOf(word)>bottom)
		    	 bottom = bottomOf(word);
		return bottom;
	}
	
	protected float findBottomMargins (List<List<Block>> margins) {
		Float bottom = bottomOf(margins.get(0).get(0));	
		for (List<Block> line:margins)
			for (Block word:line)
		      if (bottomOf(word)>bottom)
		    	  bottom = bottomOf(word);
		return bottom;
	}
		
	protected List<Block> stripHandWritings (List<Block> words,String pageType) {
		ListIterator<Block> itr = words.listIterator();
	    while (itr.hasNext()) {
	    	Block word = itr.next();
	    	if (pageType.equals("Odd") && word.getTextType().equals("HANDWRITING") && ! word.getText().matches("[6|$|&][.].*|..?[.]")) 
	    		itr.remove();
	    	else if (pageType.equals("Even") && word.getTextType().equals("HANDWRITING") && ! word.getText().matches("[6|$][.].*|..?[.]")) 
	    	    itr.remove();
	    	else break;
	    }
		return words;
	}
	
	
	
	protected List<Block> extractMiddleHandWritings(List<Block> lines, List<Block> words,float margin,String pageType) {
		ListIterator<Block> itr = lines.listIterator();
		whileloop:
		while(itr.hasNext()) {
			Block line = itr.next();
			if(topOf(line) >= footerMargin) break;
			List<Block> lineWords = findLineWords(line,words,margin,pageType);
			if (lineWords.size() == 1) continue;
			for (Block word:lineWords)
				if(!word.getTextType().equals("HANDWRITING")|| word.getText().matches("[A|I|F][.].*") || word.getText().matches(marginPatterns))
					continue whileloop;
			this.middleHandWritings.add(line);
			itr.remove(); 
		}
		return middleHandWritings;
	}
	
	protected List<Block> cleanMargins(List<Block> margins) {
        Iterator<Block> itr = margins.iterator();
        while (itr.hasNext()) {
     	   Block word = itr.next();
     	   for (String pattern:filteringPatterns)
     		   if (word.getText().matches(pattern))
     			   itr.remove();
        }
		return margins;
	}
	
	
	protected List<LinkedPlantsToMargins> linkPlantsToMargin(List<List<Block>> plants, List<List<List<Block>>> marginParagraphs) throws IOException {
		
		List<LinkedPlantsToMargins> plantsToMargins = new ArrayList<>();
		
		for (List<Block>plant:plants) {
			if(plant.isEmpty()) continue;
			LinkedPlantsToMargins linkedPlantsToMargins = new LinkedPlantsToMargins();
			linkedPlantsToMargins.setPlantWords(plant);
			plantsToMargins.add(linkedPlantsToMargins);
		}
		
		for (List<List<Block>>marginParagraph:marginParagraphs) {
			if (marginParagraph.isEmpty()) continue;
			float marginTop = topOf(marginParagraph.get(0).get(0));
		    float shortDistance = 1000;
		    LinkedPlantsToMargins target = null;
            for (LinkedPlantsToMargins ptm:plantsToMargins) {
            	
           	 float distance = marginTop - topOf(ptm.getPlantWords().get(0));
           	 if ((distance >= 0 && distance < shortDistance) 
           			 || Math.abs(distance) <0.015 
           			 || (Pattern.compile("^[a-zA-Z]").matcher(textOf(marginParagraph.get(0).get(0))).find() && Math.abs(distance)<0.025)) {
           		 shortDistance = Math.abs(distance);
           		 target = ptm;
           	 }
            	 
            }
            if (target!=null)
            target.getMargins().addAll(marginParagraph);
		}
		   
	        return plantsToMargins;
	}
	
	protected void produceImages (String pageType,String documentPath) throws IOException {
		
		int segNo=0;
		String prefix = volNo + "-" + pageNo + "-";
		
		String croppedImagePath = new File(documentPath).getParent() + Utilities.croppedOutputFolder+pageNo+"/";
		String markedImagePath = new File(documentPath).getParent() + Utilities.markedOutputPathFolder+pageNo+"/";
		
		new File(croppedImagePath).mkdirs();
		new File(markedImagePath).mkdirs();
		
		Utilities.deleteImages(croppedImagePath);
		Utilities.deleteImages(markedImagePath);
		
		BufferedImage markedPage = image;
		
		
		 if (headers.size()> 0) {
	         Utilities.cropImage(image,pageLeft,pageTop,pageWidth,headerMargin - pageTop + 0.005f, croppedImagePath + prefix + "header.jpg");
	         BufferedImage markedImage = Utilities.drawRectangle(image, pageLeft, pageTop, pageWidth,headerMargin - pageTop + 0.005f);
	         markedPage = Utilities.drawRectangle(markedPage, pageLeft, pageTop, pageWidth,headerMargin - pageTop + 0.005f);
	         Utilities.reduceImageSize(markedImage,compression,markedImagePath+prefix+"header-d.jpeg");
		 }
		
		
		for (LinkedPlantsToMargins ptm: plantsToMargins) {
			List<Block> plantWords = ptm.getPlantWords();
			List<List<Block>> margins = ptm.getMargins();
			if (plantWords.get(0).getText().startsWith("14")) continue;
			float topPlant = findTopPlant(plantWords);
			float hieghtPlant = findBottomPlant(plantWords) - topPlant + 0.005f;
		
			if (ptm.getMargins().size() > 0) {
		         float topMargins = topOf(margins.get(0).get(0));		         
		         topMargins = topPlant < topMargins ? topPlant : topMargins;
		         float hieghtMargins = findBottomMargins(margins) - topMargins + 0.005f;
		         Utilities.cropImage(image,leftMargin,topPlant,rightMargin - leftMargin +0.015F,hieghtPlant,croppedImagePath+ prefix + ++segNo +"-plant.jpg");
		         
			     if (pageType.equals("Even"))
			          Utilities.cropImage(image,pageLeft,topMargins,leftMargin - pageLeft,hieghtMargins,croppedImagePath+ prefix + segNo + "-margin.jpg");
			     else
			          Utilities.cropImage(image,rightMargin,topMargins,pageLeft + pageWidth - rightMargin,hieghtMargins,croppedImagePath+ prefix + segNo + "-margin.jpg"); 
			    
			     BufferedImage markedImage = Utilities.drawRectangle(image, leftMargin, topPlant, rightMargin - leftMargin, hieghtPlant);
			     markedPage =  Utilities.drawRectangle(markedPage, leftMargin, topPlant, rightMargin - leftMargin, hieghtPlant);
			     if (pageType.equals("Even")) {
			         markedImage = Utilities.drawRectangle(markedImage, pageLeft, topMargins, leftMargin - pageLeft,hieghtMargins);
			         markedPage = Utilities.drawRectangle(markedPage, pageLeft, topMargins, leftMargin - pageLeft,hieghtMargins);
			     }
			     else {
			    	 markedImage = Utilities.drawRectangle(markedImage, rightMargin, topMargins,pageLeft + pageWidth - rightMargin,hieghtMargins);
			    	 markedPage = Utilities.drawRectangle(markedPage, rightMargin, topMargins,pageLeft + pageWidth - rightMargin,hieghtMargins);
			     }
			     Utilities.reduceImageSize(markedImage,compression,markedImagePath+ prefix + segNo +"-plant&margin-d.jpeg");
			}
			else {
				Utilities.cropImage(image,leftMargin,topPlant,rightMargin - leftMargin +0.015F,hieghtPlant,croppedImagePath+ prefix + ++segNo + "-plant.jpg");
				BufferedImage markedImage = Utilities.drawRectangle(image, leftMargin, topPlant, rightMargin - leftMargin, hieghtPlant);
				markedPage = Utilities.drawRectangle(markedPage, leftMargin, topPlant, rightMargin - leftMargin, hieghtPlant);
				Utilities.reduceImageSize(markedImage,compression,markedImagePath+ prefix + segNo+ "-plant-d.jpeg");
			}
		}
		

		  if (getFottersSize()>3) {
				float footerMargin = footers.get(0).getGeometry().getPolygon().get(0).getY();
			    Utilities.cropImage(image,pageLeft,footerMargin,pageWidth,pageBottom - footerMargin + 0.004f,croppedImagePath+ prefix+"footer.jpg");
			    BufferedImage markedImage = Utilities.drawRectangle(image,pageLeft,footerMargin,pageWidth,pageBottom-footerMargin+0.004f);
			    markedPage = Utilities.drawRectangle(markedPage,pageLeft,footerMargin,pageWidth,pageBottom-footerMargin+0.004f);
			    Utilities.reduceImageSize(markedImage,compression,markedImagePath+ prefix +"footer-d.jpeg");
			}	
		  
		  Utilities.reduceImageSize(markedPage,Utilities.getCompression(markedPage),markedImagePath+ prefix +"d.jpeg");
	}
	
	
	protected BufferedImage cropImage(BufferedImage image, float left, float top, float width, float height, String fileName) throws IOException {
		   BufferedImage img = image.getSubimage(Math.round(image.getWidth() * left), Math.round(image.getHeight() *top),
				                                 Math.round(image.getWidth()* width), Math.round(image.getHeight()*height));
		   File outputfile = new File(fileName);
		   ImageIO.write(img, "jpg", outputfile);
		   return img;
		}
		
	
	protected List<Block> topSort(List<Block> blocks) {
		blocks.sort((blk1, blk2) -> {
			if (topOf(blk1) > topOf(blk2)) return  1;   
			else if (topOf(blk1) < topOf(blk2)) return -1;
			else return 0; });
		return blocks;
	}
	
	protected List<Block> topSortLineWords(List<Block> blocks,float margin,String pageType) {
		blocks.sort((blk1, blk2) -> {
			if ( topOf(findLineWords(blk1,words,margin,pageType).get(0)) > topOf(findLineWords(blk2,words,margin,pageType).get(0))) return  1;   
			else if (topOf(findLineWords(blk1,words,margin,pageType).get(0)) <topOf(findLineWords(blk2,words,margin,pageType).get(0))) return -1;
			else return 0; });
		return blocks;
	}
	
	
	protected List<List<Block>> topSortMarginWords(List<List<Block>> blocks) {
		blocks.sort((blk1, blk2) -> {
			if (topOf(blk1.get(0)) >topOf( blk2.get(0))) return  1;   
			else if (topOf(blk1.get(0)) < topOf(blk2.get(0))) return -1;
			else return 0; });
		return blocks;
	}
	
	
	protected List<Block> leftSort(List<Block> blocks) {
		blocks.sort((blk1, blk2) -> {
			if (leftOf(blk1) > leftOf(blk2)) return  1;   
			else if (leftOf(blk1) < leftOf(blk2)) return -1;
			else return 0; });
		return blocks;
	}
	
	protected int getFottersSize() {
		int c = 0;
		for(Block line:footers) {
		   c+=findLineWords(line,words,5000,"Odd").size();
		   if (c>20) return c;
		}
		return c;
	}
	
	
	protected Float findTopLineWords(List<Block> lineWords, Float top) {
	    Block temp = lineWords.get(0);
	    
	    if (lineWords.size()>1 && topOf(lineWords.get(1)) - topOf(temp) > 0.005)
	    		temp = lineWords.get(1);
	    
	    if (topOf(temp) > top)
	    	top = topOf(temp);
	    
		return top;
	}
	
	protected Float findLeftLineWords(List<Block> lineWords, Float left) {
		if (leftOf(lineWords.get(0)) - left < 0.13)
		    left = leftOf(lineWords.get(0));
		return left;

	}
	
	
	protected float leftOf(Block block) {
		return block.getGeometry().getBoundingBox().getLeft();
	}
	protected float rightOf(Block block) {
		return block.getGeometry().getBoundingBox().getLeft() + block.getGeometry().getBoundingBox().getWidth();
	}	
	protected float topOf(Block block) {
		return block.getGeometry().getBoundingBox().getTop();
	}
	protected float heightOf(Block block) {
		return block.getGeometry().getBoundingBox().getHeight();
	}
	protected float bottomOf(Block block) {
		return block.getGeometry().getPolygon().get(3).getY();
	}	

	protected float widthOf(Block block) {
		return block.getGeometry().getBoundingBox().getWidth();
	}	
	
	protected String textOf(Block block) {
		return block.getText();
	}	
	
}