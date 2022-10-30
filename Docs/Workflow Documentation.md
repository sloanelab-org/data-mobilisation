# Mobilising Digitised Images from John Ray's *Historia Plantarum* 

## Aim ##

Mobilise data from Sloane’s copy of John Ray, Historia Plantarum, including plant names and annotations, to be absorbed into the NHM’s data system,
and then into the SloaneLab Knowledge base, thereby creating a digital ‘way in’ to the NHM’s historical botanical collection and a searchable tool.

## Overview ##
John Ray’s Historia Plantarum was published in three volumes between 1686 and 1704. The NHM currently holds Hans Sloane’s personal copy of this publication
which contains handwritten notations surrounding the printed text. These notes represent specimens in Sloane’s botanical collection 
(including volume and page numbers) and have been written in a variety of different hands. These notes were added over a number of years. 
In order to find a botanical specimen in Sloane’s collection today, we must physically look in this copy of Historia Plantarum. 
In other words, there is no digital version of this finding aid. Images exist of all three NHM volumes, Volume III images are currently only on MAM

## Requirements ##

*	Find printed page and volume number
*	Extract printed Ray plant name (polynomial, common name, referenced material/abbreviations)
*	Extract the hand-written annotations/description of herbarium volume and folio
*	Extract the hand-written annotations of plant names and herbarium details (found in header and footer of printed page)
*	Capturing the relationship between the printed plant names and specimens in the annotations

<p align="center">
<img src="https://user-images.githubusercontent.com/30471029/198874100-85a2c604-8a94-4afc-8dfc-882a16aeefb9.png"/>
</p>

## Method and Process Workflow ##

To automate data mobilisation, we developed the processing workflow shown below. We approach the mobilising task in three stages. The first stage is **Data Extraction**, in which it finds relevant text segments and captures the relationships between them using Optical Character Recognition (OCR) technique. The second stage is **Image Transcription**, where the extracted segments from the first stage are presented to human reviewers to transcribe the handwritten numbers on margins. The final stage is **Natural Language Processing**, in which the data from the first and second stages are consolidated and processed through an Natural Language Processing (NLP) pipeline.

![DataMobilisationWF](https://user-images.githubusercontent.com/30471029/198874961-28218e6e-f3aa-4ce8-9c0e-a36c121e1ad6.png)


### 1. Data Extraction ###

The input for this stage is a digitised image which represents one page of Historia Plantaurm book. To implement the Data extraction stage we developed a web service tool using JAVA language. The service incorporates Textract which is an OCR cloud service provided by AWS. Data extraction is executed in two steps:

* **Document Analysis**: In this step we use Textract to recognise the document structure. Textract is a machine learning service that automatically extracts text, handwriting and data from generic structured or unstructured documents. When Textract processed a document, it returns the text detected  from the document in a list of items including the lines, the words, and the location of each item. 

* **Segments Identification**:
In the second step, the tool processes the output returned from Textract to extract the requried segments along with the relationships. It analyses the information obtained from Textract to capture the layout of the page. It makes observation based on the geometry points of lines and words to recognise headers, footers, plant names and the handwritten annotations linked to the plant names. The output of the tool is a JSON file containing transcriptions of the relevant data and images highlited with rectangles boxes around each text segments as illusterated in the image below.


<p align="center">
  <img src="https://user-images.githubusercontent.com/30471029/198893227-8ab68021-5912-4f80-ad13-708c866816f2.png"/>
</p>

The tool also crops the releveant segments and stores them in a corpus of annotated images for further processing. For example, the iamges below cropped from Volume I page 590 

<p align="center">
<img src="https://user-images.githubusercontent.com/30471029/198893288-10e67fe9-7cf9-4373-a0ba-0db7796c7f8b.png" />
  </br>
  Sloan's Hand writings 
</p>
 <p align="center">         
<img src="https://user-images.githubusercontent.com/30471029/198893295-5f7e9ee5-7fb7-4314-be79-a85328690b29.png"/>
  </br>
  Plant name
</p>


### 2. Image Transcription ###
In this stage, we build a project on Zooniverse platform to verify the output produced in the previous stage and to transcribe the Sloan's hand writing on margins. The project is private and only trained human reviewers can access the project to view and transcribe the images. Zooniverse allows users to export annotated images in JSON format. The image below shows a snippet of the annotation workflow on Zooniverse.

<p align="center">
  <img src="https://user-images.githubusercontent.com/30471029/198894393-f69edc86-e7e6-4fa0-963b-5a487cbb6789.png" />
  </p>


### 3. Natural Language Processing ###

The output obtained from the second stage is mapped and consolidated against the output produced from the data extraction stage to create one comprehensive dataset containing all required data.


