# Splitting Plant Names into single names and authors

Plant names extracted by the Data Extraction service are decomposed into their individual components, producing different types of single plant names including: Accepted Names, Synonym names and Common names, each associated with the corresponding authors.

### Configure the service

* The service is running on port:7080.\
The port can be changed using the "server.port" property in the appliecation.properties file located in the resource folder.


### Run the service

* To split and update each plant name with its individual components, use the following URL where "file" points to the location of the plant names file
http://localhost:7080/splitPlantNames?file=path/to/plantNamefile/

