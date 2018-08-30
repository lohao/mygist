package com.hikvision.lohao.demo.traffic_forecasting

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Column, DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{LongType, TimestampType}
import org.apache.spark.sql.expressions.Window


object traffic_predict {
  val spark = SparkSession.builder().master("local").getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  def loadData(): DataFrame = {
    spark.read.format("csv").option("header", "true").load("D:\\project\\AlgorithmTemplateTest\\data\\trafficPredict.csv")
  }

  //add year/month/day/hour/minute/second column
  def splitDateTime(df: DataFrame, timeColumn: String): DataFrame = {
    //todo 校验
    var dataFrame = df.withColumn(timeColumn, unix_timestamp(df(timeColumn), "yyyy-MM-dd HH:mm:ss").cast(TimestampType))

    dataFrame = dataFrame.withColumn("year", year(dataFrame(timeColumn)))
    dataFrame = dataFrame.withColumn("month", month(dataFrame(timeColumn)))
    dataFrame = dataFrame.withColumn("day", dayofmonth(dataFrame(timeColumn)))

    //todo 增加星期几
    //    dataFrame = dataFrame.withColumn("week", )
    dataFrame = dataFrame.withColumn("hour", hour(dataFrame(timeColumn)))
    dataFrame = dataFrame.withColumn("minute", minute(dataFrame(timeColumn)))
    //    dataFrame = dataFrame.withColumn("second", second(dataFrame(timeColumn)))

    dataFrame
  }

  def removeDuplicateRows(dataFrame: DataFrame, columns: Array[String]): DataFrame = {
    dataFrame.dropDuplicates(columns)
  }

  def groupByColumnWithOp(dataFrame: DataFrame, columns: Array[String], statisticColumn: String, func: String): DataFrame = {
    var df = dataFrame
    func match {
      case "sum" => df = dataFrame.groupBy(columns.map(dataFrame(_)): _*).sum(statisticColumn)
      case "avg" => df = dataFrame.groupBy(columns.map(dataFrame(_)): _*).avg(statisticColumn)
      case "min" => df = dataFrame.groupBy(columns.map(dataFrame(_)): _*).min(statisticColumn)
      case "max" => df = dataFrame.groupBy(columns.map(dataFrame(_)): _*).max(statisticColumn)
      case "count" => df = dataFrame.withColumn("count", count(columns(0)).over(Window.partitionBy(columns(0), columns.toSeq.tail:_*)))
      case _ => println(s"groupBy not support [${func}] for [${statisticColumn}]")
    }

    df
  }

  def unionDataframeForDay(dataFrame: DataFrame, currentRowNum: Int, unionDf: DataFrame): DataFrame = {
    if (dataFrame.count() == currentRowNum)
      return unionDf
    val currentRow: Row = dataFrame.take(currentRowNum).last
    val tempDf = dataFrame.filter(row => {
      row.get(1) == currentRow.get(1) && row.get(2) == currentRow.get(2) && row.get(3) == currentRow.get(3)
    })
    unionDataframeForDay(dataFrame, currentRowNum + 1, unionDf.union(tempDf))
  }


  def main(args: Array[String]): Unit = {
    val oriData = loadData()

    //chinese transfer to english and select valid column
    val lookup = Map("车牌号码" -> "Lpn", "经过时间" -> "Pt", "卡口名称" -> "Bn")
    var data = oriData.select(oriData.columns.filter(lookup.contains).map(c => oriData.col(c).alias(lookup.getOrElse(c, c))): _*)

    data = data.withColumn("Bn", data("Bn").cast(LongType))

    //order by Pt
    data = data.orderBy(data.col("Pt"))

    //time split
    data = splitDateTime(data, "Pt")

    //    println(s"before remove duplicate columns count: ${data.count()}")

    //data remove duplicate
    val duplicateColumns = Array("Lpn", "year", "month", "day", "hour", "minute")
    data = removeDuplicateRows(data, duplicateColumns)
    //    println(s"after remove duplilcate columns count: ${data.count()}")

    //groupby
    val groupByColumns1 = Array("Bn", "year", "month", "day", "hour")
    val func = "count"
    val statisticColumn = "Bn"
    data = groupByColumnWithOp(data, groupByColumns1, statisticColumn, func)
    //    println(s"groupby count: ${data.count()}")

    data.cache()

    //rowwindows,考虑到流量预测的场景比较特殊，不能按照row的方式滑窗
    //    val groupByColumns2 = "Bn"
    //    val orderByColumns = Array("year", "month", "day", "hour")
    //    val byBn = Window.partitionBy(groupByColumns2).orderBy("year", "month", "day", "hour").rowsBetween(-14, 0)
    //    data = data.withColumn("windows14_avg", avg("count").over(byBn))
    //    data = data.withColumn("windows14_max", max("count").over(byBn))
    //    data = data.withColumn("windows14_min", min("count").over(byBn))

    //通过叠加的方式构建新的
    //    data = unionDataframeForDay(data, 1, data.sparkSession.createDataFrame(data.sparkSession.sparkContext.emptyRDD[Row], data.schema))



    data.show(50)
    data.describe()
    data.printSchema()
  }


  /**
    * Gets the nth percentile entry for an RDD of doubles
    *
    * @param inputScore : Input scores consisting of a RDD of doubles
    * @param percentile : The percentile cutoff required (between 0 to 100), e.g 90%ile of [1,4,5,9,19,23,44] = ~23.
    *                   It prefers the higher value when the desired quantile lies between two data points
    * @return : The number best representing the percentile in the Rdd of double
    */
  def getRddPercentile(dataFrame: DataFrame, column: String, percentile: Double): Double = {
    val numEntries = dataFrame.count().toDouble
    val retrievedEntry = (percentile * numEntries / 100.0).min(numEntries).max(0).toInt

    dataFrame
      .select(column)
      .rdd
      .sortBy(row => row.get(0).asInstanceOf[Int])
      .zipWithIndex()
      .filter { case (score, index) => index == retrievedEntry }
      .map { case (score, index) => score }
      .collect()(0).get(0).asInstanceOf[Double]
  }

}
