package com.hikvision.lohao.demo

import com.hikvision.lohao.demo.test.ConfigurableWordCount
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.SparkSession

object SelfPipelineDemo extends App {
  val ss = SparkSession.builder().master("local").appName("SelfPipelineDemo").getOrCreate()
  ss.sparkContext.setLogLevel("error")

  val sqlCtx = ss.sqlContext

  val oriRdd = ss.sparkContext.textFile(args(0)).map(_.split("\t")).map(eachRow =>
    (eachRow(0), eachRow(1).toString)
  )

  val msgDF = sqlCtx.createDataFrame(oriRdd).toDF("label", "message")

  val configurableWordCount = new ConfigurableWordCount()
    .setInputCol("message")
    .setOutputCol("result")

  val pipeline = new Pipeline().setStages(Array(configurableWordCount))
  val model = pipeline.fit(msgDF)

  val result = model.transform(msgDF)

  result.printSchema()
  result.show(10, false)

}
