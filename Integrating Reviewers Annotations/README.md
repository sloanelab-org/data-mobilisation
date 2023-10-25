

# Integrating Reviewers' Annotations into Plants dataset

This service processes JSON files extracted from participants’ work on Zoniverse and update the plant name set extracted from Historia Plantarum with the Data Extraction service. This includes adding/removing plant name, updating the relationships that links plant names to margins, and integrating the handwritten transcriptions


### Configure the service

* The service is running on port:7078.\
The port can be changed using the "server.port" property in the appliecation.properties file located in the resource folder.


### Run the service

* To integrate reviewers work from workflow 1 annotations into plants dataset, use the following URL where "annotationsFile" points to the JSON file produced by Parsing Zooniverse Files service, "outputFile" refers to the location of the output file, "vol" indicates the volume to which the annotations pertain.
http://localhost:7078/integrateWF1?annotationsFile=path/to/Annotations File.json&outputFile=path/to/outputFilelocation.json&vol=I

* To integrate reviewers work from workflow 2 annotations into plants dataset, use the following URL where "annotationsFile" points to the JSON file produced by Parsing Zooniverse Files service, "outputFile" refers to the location of the output file, "vol" indicates the volume to which the annotations pertain.
http://localhost:7078/integrateWF2?annotationsFile=path/to/Annotations File.json&outputFile=path/to/outputFilelocation.json&vol=II

