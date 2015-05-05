This tutorial assumes that you have the PredictionIO QuickStart Recommendation Engine up and running locally on your machine

# CoreNLP - Stanford Classifier

Go to http://nlp.stanford.edu/software/classifier.shtml and download the Stanford Classifier.

Create a lib folder in your predictionio project folder, and drop stanford-classifier.jar (located in the top level of Stanford Classifier) into the folder.  

For this project we will be making extensive use of the Stanford Classifier.  The Stanford Classifier is an NLP tool that classifies an object into one of several categories based on the training data.

Input to the Stanford Classifier takes the form of
<classification>\t<text>\t<text>\t…\t<text>   (Note that the <> characters are just acting as delimiters, you don’t need it in the actual input)

The classification can be numerical or textual, but cannot contain spaces.
The text can contain anything, including spaces.

As a slight detour to gain a better understanding of the Stanford Classifier you can attempt to run it on it’s own (and not as part of the Prediction Engine).  This wiki page will take you on a brief run through of how the Stanford Classifier works. 

One important thing about the Stanford Classifier that you need to know is how to specify properties, i.e. how the Stanford Classifier will parse your data.  This can be done programmatically or using a properties file.  The specifications are given in the form <column number>.<specification(=value)>.  Columns are 0 indexed, and the 0th column is assumed to be the classification.

Example properties file:
1.useSplitWords=true
1.splitWordsRegexp=\s

In the above example we specify that the 1st column uses split words (has spaces) and that the regular expression for spaces is \s.

A second thing to note is that when taking queries, the classifier will only take queries of the same form as the input.  This means that if you don’t have a gold standard you will need to prefix your data with \t.  For example: \t<text>\t<text> would be a valid query if the original input was of the form <classification>\t<text>\t<text>

#Integrating CoreNLP into PredictionIO

The integration of Core-NLP into PredictionIO isn’t particularly complicated but involves several steps. 

Data Importation:

Throughout this part of the guide we will be using a generic dataset with input of the form <classification>::<text>::<gender> as an example.  You can follow along using any dataset.

As always when using a new dataset or engine customization we will need to change our import file in order to import the type of data that we want to parse, by modifying the import_eventserver.py script (or whichever import script you are using).  

A generic change for a dataset of the form classification::textual data::gender would be as follows:

```
client.create_event(
  event = “twitter”,
  entity_type = “question”,
  entity_id=data[0], //this is where the classification goes
  properties={“text”:data[1], “gender”:data[2]} //this is where your data goes
)
```

In order to customize this for your own dataset, simply use the appropriate column from your dataset (0 indexed) for the entity_id and properties, and add or delete to as many properties as you need.  By default the import_eventserver.py script uses :: as a delimiter, but you are free to change it to whatever best suits your needs.

Data Pipeline:

In order to use CoreNLP we need to make several big changes to our data pipeline.  
First we will need to make modifications to that we can read in our new data.  To do this we will need to modify the DataSource, Engine, Algorithm, and Model.

DataSource.scala

We will need to modify our RDD class to accept strings as both the label and the corresponding text.  This is because the CoreNLP classifier treats labels (even numeric ones) as text: 

```
case class TextClass(
  val classification: String,
  val text: String,
  val gender: String
)

class TrainingData(
  val texts: RDD[TextClass]
) extends Serializable {
  override def toString = {
    s”queries: [${texts.count()}] (${texts.take(1).toList}...)”
  }
}
```

  We will also need to modify how we read data in due to the changes we made in the data importation and the RDD:
  entityType = Some(“question”)
  eventNames = Some(List(“twitter”)))(sc)

```
val text = try {
  val textValue: String = event.event match {
  case "twitter" => event.properties.get[String]("text")
  case _ => throw new Exception(s"Unexpected event ${event} is read.")
}
val genderValue: String = event.event match {
  case "twitter" => event.properties.get[String]("gender")
  case _ => throw new Exception(s"Unexpected event ${event} is read.")
}
TextClass(event.entityId,
          textValue,
          genderValue)
} catch {
  case e: Exception => {
    logger.error(s"Cannot convert ${event} to TextClass. Exception: ${e}.")
    throw e
  }
}
text
```

Engine.scala

We will need to change our Query and PredictedResult class to the following to account for the labels being Strings:

                            case class Query(
                              val text: String
                              ) extends Serializable

                              case class PredictedResult(
                                val queryResults: String,
                                  val gender: Option[Set[String]]
                                  ) extends Serializable


                                  Model.scala

                                  The Model that we will need is very simple.  All the model needs to do is save a classifier.  Delete all the contents of the Model file and replace it with the following:

                                  import edu.stanford.nlp.classify.Classifier
                                  import edu.stanford.nlp.classify.ColumnDataClassifier

                                  The Model will only contain a Classifier:

                                  class Model(val cl: Classifier[String, String])

                                  We will need to change the save, toString and apply functions appropriately:

                                    1 package org.apache.spark.mllib.classification
                                      5 
                                        6 import org.template.classification.AlgorithmParams
                                          7 
                                            8 import io.prediction.controller.IPersistentModel
                                              9 import io.prediction.controller.IPersistentModelLoader
                                               10 
                                                11 import org.apache.spark.SparkContext
                                                 12 
                                                  13 import edu.stanford.nlp.classify.Classifier
                                                   14 import edu.stanford.nlp.classify.ColumnDataClassifier
                                                    15 
                                                     16 class Model(
                                                      17     val cl: Classifier[String, String])
                                                      18   {
                                                         19   def save(id: String, params: AlgorithmParams,
                                                          20     sc: SparkContext): Boolean = {
                                                             21     sc.parallelize(Seq(cl)).saveAsObjectFile(s"/tmp/${id}/cl")
                                                              22     true
                                                               23   }
                                                                24 
                                                                 25   override def toString = {
                                                                    26     s"empty"
                                                                     27   }
                                                                      28 }
                                                                       29 
                                                                        30 object Model
                                                                         31   extends IPersistentModelLoader[AlgorithmParams, Model] {
                                                                            32   def apply(id: String, params: AlgorithmParams,
                                                                             33     sc: Option[SparkContext]) = {
                                                                                34     new Model(
                                                                                 35       cl = sc.get
                                                                                  36         .objectFile[Classifier[String, String]](s"/tmp/${id}/cl").first
                                                                                   37     )
                                                                                 38   }
                                                                                  39 }

                                                                                  Algorithm.scala

                                                                                  Train
                                                                                  The easiest way to train the data is to save all of our data as a tab separated list of inputs and read that into our Classifier.

                                                                                  var labelList = data.texts.map(_.text_type).collect();
                                                                                  var textList = data.texts.map(_.text).collect();
                                                                                  var genderList = data.texts.map(_.gender).collect();
                                                                                  val pw = new PrintWriter("corenlpData")

                                                                                  for (x <- 0 to data.texts.count().toInt-1) {
                                                                                      pw.print(labelList(x) + "\t" + textList(x) + "\t" + genderList(x) + "\n")
                                                                                  }

                                                                                  val classifier = cdc.makeClassifier(cdc.readTrainingExamples("corenlpData"))
                                                                                  new Model(cl = classifier)

                                                                                  Predict

                                                                                  /* Read in our query and modify it into a form that the classifier can read*/
                                                                                  val d = cdc.makeDatumFromLine(“\t” + query.text + “\t” + query.gender)

                                                                                  /* Output the result*/
                                                                                  new PredictedResult(query.text + “ ⇒ “ + model.cl.classOf(d)


