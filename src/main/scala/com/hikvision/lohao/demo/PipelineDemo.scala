package com.hikvision.lohao.demo

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.{IndexToString, VectorAssembler}
import org.apache.spark.sql.SparkSession

object PipelineDemo extends App {

  val ss = SparkSession.builder().master("local").appName("pipelineDemo").getOrCreate()
  ss.sparkContext.setLogLevel("error")

  import ss.implicits._
//  val data = ss.read
//      .option("seq", ",")
//      .option("inferSchema", "true")
//      .option("header", "true")
//      .csv("D:\\project\\AlgorithmTemplateTest\\data\\dbdata.csv")

  val oriData = ss.sparkContext.textFile("D:\\project\\AlgorithmTemplateTest\\data\\dbdata.csv").map(_.split(","))
    .map(row => {
      (row(0).toDouble, row(1).toDouble)
    })

  val data = ss.sqlContext.createDataFrame(oriData).toDF("x", "y")

  val assembler = new VectorAssembler()
    .setInputCols(Array("x", "y"))
    .setOutputCol("features")


  val km = new KMeans()
    .setK(3)
    .setMaxIter(100)

//  val labelConverter = new IndexToString()
//    .setInputCol("prediction")
//    .setOutputCol("predictedLabel")

  val Array(trainData, testData)  = data.randomSplit(Array(0.99, 0.01))

  val pipeline = new Pipeline().setStages(Array(assembler, km))
  pipeline.getStages.foreach(println)

  val model = pipeline.fit(trainData)

  model.write.overwrite().save("D:\\project\\AlgorithmTemplateTest\\data\\kmeanModel")

  val sameModel = PipelineModel.load("D:\\project\\AlgorithmTemplateTest\\data\\kmeanModel")


  val predictions = sameModel.transform(testData)
  predictions.show()
  predictions.printSchema()

}
