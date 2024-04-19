# Spell Checking OCRed Plant Names

Plant names OCRed by Amazon Textract cloud service are undergo spellchekcing to improve their accuracy.

### Configure the service

* The service is running on port:7081.\
The port can be changed using the "server.port" property in the appliecation.properties file located in the resource folder.


### Run the service

* To spellcheck each plant name, use the following URL where "file" points to the location of the plant names JSON file, "from" indicates the first node where spellchecking begins
http://localhost:7081/spellCheck?file=path/to/plantNamefile/&from=0
