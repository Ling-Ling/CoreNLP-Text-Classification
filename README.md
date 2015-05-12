#CoreNLP Text Classification Engine

This engine uses Stanfords CoreNLP library to classify strings.  The engine will place query strings into a class based on the sample data. 

##Sample Data

Data files should be formatted as classification::text.  Where text can be any arbitrary string (with or without spaces), and classification is a string (without spaces) 
```
$ python data/import_eventserver.php --access_key <your_access_key>
```

##Queries and Results

Queries must contain a "text" that should be populated using the string you want classified.  In addition to this required field you can add the optional field "gender" to your query where gender takes "male" and "female".  The result contains a single field "queryResults" which will contain your answer in the following format: query ==> classification

Simple query with only text
```
$ curl -H "Content-Type: application/json" -d '{ "text": "classify me" }' http://localhost:8000/queries.json
$ {"queryResults":"classification ==> class"}
```

Query with text and some optional arguments
```
$ curl -H "Content-Type: application/json" -d '{ "text": "classify me", "gender": "female" }' http://localhost:8000/queries.json
$ {"queryResults":"classification: class"}
```








