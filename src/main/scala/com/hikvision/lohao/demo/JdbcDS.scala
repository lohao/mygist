package com.hikvision.lohao.demo

import org.apache.spark.sql.SparkSession

object JdbcDS extends App{

  val spark = SparkSession.builder().master("local[*]").appName("jdbcds").getOrCreate()

  val tableStr = "(select * from (select temp1.*, rownum as rn from ZHCX_BDQ.BDQ_CXB_QG temp1)) temp"

  val df = spark.read.format("jdbc")
    .option("url", "jdbc:oracle:thin:@10.3.70.116:1521:xe")
    .option("dbtable", tableStr)
    .option("user", "YCX_XXZX")
    .option("password", "YCX_XXZX")
    .option("fetchsize", 1000)
    .option("partitionColumn", "rn")
    .option("lowerBound", "0")
    .option("upperBound", "1000")
    .option("numPartitions", 10)
    .option("driver", "oracle.jdbc.driver.OracleDriver")
    .load()

  println(s"count = ${df.count()}, partition size = ${df.rdd.getNumPartitions}")

  df.printSchema()
  df.rdd.mapPartitionsWithIndex((index, it) => {
    var count = 0
    while (it.hasNext) {
      count += 1
      it.next()
    }
    println(s"partition ${index} has ${count}")
    Iterator.empty
  }).count()

  Thread.sleep(10000000)
}
