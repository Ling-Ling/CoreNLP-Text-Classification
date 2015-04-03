Sample Data

Data files should be formatted as classification::text.  Where text can be any arbitrary string (with or without spaces), and classification is a string (without spaces) 
```
python data/import_eventserver.php --access_key <your_access_key>
```

Query

Queries are any string that you want classified.

```
curl -H "Content-Type: application/json" -d '{ "text": "classify me" }' http://localhost:8000/queries.json
```




