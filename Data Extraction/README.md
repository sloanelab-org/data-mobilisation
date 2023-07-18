# Data Extraction Service

This service extracts information from the digitised images of John Ray's Historia Plantarum Book. It submits each image (which represnts one page of the book) to Amazon Textract service to get text transcriptions and coordinates of the page's layout.

It processes the results returend by Textract to:

* Find printed page number. 
* Extract printed Ray plant name.
* Extract handwritten annotations of plant names (found in headers and footers).
* Capture the relationship between the printed plant names and specimens in the annotations.

### Configure the service

* The service is running on port:7070.\
The port can be changed using the "server.port" property in the appliecation.properties file located in the resource folder.

* Update AWS ceredintials \
The following properties in the application.properties need to be updated according to the user's credentials
  * cloud.aws.region
  * cloud.aws.credentials.accessKey
  * cloud.aws.credentials.secretKey


### Run the service

* To process one image, use the following URL where "image" points to the image file path and "vol" indicates volume number\
http://localhost:7070/processImage?image=path/to/folder/image.jpg&vol=I

* To process a set of images in folder use the following URL where "imagesFloder" points to the folder containing the images and "vol" indicates volume number\
http://localhost:7070/processFilesFolder?imagesFolder=path/to/folder/

### Output

The service creates and populates the output in three folders 
* MarkedImages: It has a list of folders, each relates to one image and containes a set of images highlited with rectangles around the extracted segments
* CroppedImages: It has a list of folders, each relates to one image and containes a set of cropped images represent a text segment
* processedImages: It containes all images that were processed succefully.
