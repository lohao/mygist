package com.hikvision.lohao.demo

import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.sql.SparkSession

object KmeansSC extends App {

  val spark = SparkSession.builder().master("local").getOrCreate()
  val dataset = spark.read.format("libsvm").load("D:\\project\\AlgorithmTemplateTest\\data\\sample_libsvm_data.txt")
  val kmeans = new org.apache.spark.ml.clustering.KMeans().setK(2).setSeed(1L)

  val rf = new RandomForestClassifier()
    .setLabelCol("label")
    .setFeaturesCol("features")
    .setNumTrees(10)
  rf.fit(dataset)

  val model = kmeans.fit(dataset)
  // Evaluate clustering by computing Within Set Sum of Squared Errors.
  val WSSSE = model.computeCost(dataset)
  println(s"Within Set Sum of Squared Errors = $WSSSE")

  // Shows the result.
  println("Cluster Centers: ")
  model.clusterCenters.foreach(println)
}
