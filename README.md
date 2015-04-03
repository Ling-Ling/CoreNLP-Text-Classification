Incorporating CoreNLP into PredictionIO requires a lot of changes and no small amount of work.  Be sure to carefully go over each step.  This guide assumes that you are familiar with how to install, setup and run the PredictionIO Classification Engine.

Part 1 Setup - In this section we will download all of the files we need to get started

1. Download the testClassification template from PredictionIO and ensure that it runs successfully on your computer.
2. Download the Stanford CoreNLP Classifier from this page http://nlp.stanford.edu/software/classifier.shtml.  Note that you are specifically downloading the classifier and are not downloading the entire library.
3. Inside the Stanford CoreNLP Classifier should be stanford-classifier.jar file.  Create a lib folder in the testClassification (or whatever you renamed it to) folder and copy the jar file into the lib.  As a sanity check try to import something from the library into the project and build it.

Part 2 Understanding NLP

1. After downloading NLP head to this site http://www-nlp.stanford.edu/wiki/Software/Classifier to get a feel for how NLP works.
2. Create a properties file that allows you to read and classify your data.
3. Save a training file, a test file and the properties file for your data into the data directory of the project.

Part 3 Creating the model - In this part we will write a simple model

1. Create a new file (here we called it Model.scala).  Your file should look fairly similar to the Model.scala in this example.  After this point you should still be able to build train and deploy your engine without errors.

Part 4 Using CoreNLP - In this part we will hardcode the CoreNLP classification engine into the Algorithm file just to make sure everything works.

1. In the predict method add the following code block:
  val cdc = new ColumnDataClassifier("data/propertiesfile.prop")
  val classifier = cdc.makeClassifier(cdc.readTrainingExamples("data/trainfile.train"))
  var line = "";
  for (line <- ObjectBank.getLineIterator("data/testfile.test"))
    var d = cdc.makeDatumFromLine(line)
    System.out.println(line + " ==> " + cl.classOf(d))

When you send a query to your engine the items in your test file along with their predictions shuold be output to the command line.  (Where pio deploy is running)

Part 5 Importing data to the server - In this part we will be modifying the python file while imports data into our server

Part 6 Setting up the data pipeline - In this part we will properly set up the data pipeline.
