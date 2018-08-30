package com.hikvision.lohao.demo.traffic_forecasting

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{coalesce, datediff, lag, lit, min, sum}


case class Salary(depName: String, empNo: Long, salary: Long)
object Rowwindows extends App{

  val spark = SparkSession.builder().master("local").getOrCreate()
  import spark.implicits._

  var empsalary = Seq(
    Salary("sales", 1, 5000),
    Salary("personnel", 2, 3900),
    Salary("sales", 3, 4800),
    Salary("sales", 4, 4800),
    Salary("personnel", 5, 3500),
    Salary("develop", 7, 4200),
    Salary("develop", 8, 6000),
    Salary("develop", 9, 4500),
    Salary("develop", 10, 5200),
    Salary("develop", 11, 5200)).toDF()

  val oriData = spark.read.format("csv").option("header", true).load("D:\\project\\AlgorithmTemplateTest\\data\\trafficPredict.csv")



  empsalary = empsalary.repartition(2)

  println(empsalary.rdd.getNumPartitions)
  val byEmpno = Window.orderBy('empNo).rowsBetween(-1, 0)

empsalary.withColumn("sum", sum("salary").over(byEmpno)).show()

}
