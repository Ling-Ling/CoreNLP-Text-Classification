import AssemblyKeys._

assemblySettings

name := "template-scala-parallel-recommendation"

organization := "io.prediction"

libraryDependencies ++= Seq(
  "io.prediction"    %% "core"          % pioVersion.value % "provided",
  "org.apache.spark" %% "spark-core"    % "1.2.0" % "provided",
  "org.apache.spark" %% "spark-mllib"   % "1.2.0" % "provided",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.1")
