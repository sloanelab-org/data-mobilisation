# Parsing Zooniverse CSV Files

Images highlighted with relevant segments by Data Extraction service are prepared and uploaded to the Zooniverse platform for human participants to review and transcribe the specimens annotations written on side margins. Zooniverse exports the participants’ work in JSON format embedded in CSV format along with other metadata. This parser iterates through the exported CSV file and creates JSON file containing all annotations made by the reviewer.


### Configure the service

* The service is running on port:7072.\
The port can be changed using the "server.port" property in the appliecation.properties file located in the resource folder.


### Run the service

* To create images folder to be uploaded to Zooniverse, use the following URL where "inputFolder" points to the higlighted images produced by Data Extraction Service
http://localhost:7072/imagesForWF1?inputFolder=path/to/folder/

* To create Manifest for Workflow one, use the following URL where "folder" points to the images folder created in the above URL
http://localhost:7072/manifestForwf1?folder=path/to/imageFolder/

* To parse Zooniverse output and extract reviewers' annotation from Workflow one, use the following URL where "file" points to the file exported from Zooniverse, "user" is the username who worked on the workflow, "month" and "day" indicate the date on which the work was completed
http://localhost:7072/parseZooniverseWF1?file=path/to/Zooniversefile/&user=user&month=11&day=10

* To create images folder to be uploaded to Zooniverse, use the following URL where "folder" points to the images produced by Workflow one
http://localhost:7072/imagesForWF2Manifest?folder=path/to/imageFolder/
  

* To create Manifest for Workflow two, use the following URL where "folder" points to the images folder created in the above URL
http://localhost:7072/createManifestForWF2?inputFolder=path/to/imageFolder/
 
* To parse Zooniverse output and extract reviewers' annotation from Workflow two, use the following URL where "file" points to the file exported from Zooniverse, "user" is the username who worked on the workflow, "month" and "day" indicate the date on which the work was completed
http://localhost:7072/parseZooniverseWF2?file=path/to/ZooniverseFile/&user=user&month=11&day=10
