//package com.hikvision.lohao.demo
//
//import com.hikvision.algorithmengine.algorithmtemplate.avroSchema.{WordCount, WordNum}
//import com.hikvision.algorithmengine.algorithmtemplate.template.AlgorithmTemplate
//import org.apache.avro.specific.SpecificRecord
//import org.apache.spark.SparkContext
//
//import scala.collection.JavaConversions._
//
//class WordCountDemoOld extends AlgorithmTemplate[SpecificRecord] {
//  /**
//    * @param ss   传入的sparkSession
//    * @param args 传入的算法所需参数
//    * @return
//    */
//  override def run(ss: SparkContext, args: Array[String]): SpecificRecord = {
//
//    val textFile = ss.textFile(args.apply(0))
//    val counts = textFile.flatMap(line => line.split(" "))
//      .map(word => (word, 1))
//      .reduceByKey(_ + _)
//    val result = new WordCount()
//    var seq = IndexedSeq[WordNum]()
//    val collect = counts.collect()
//    for (i <- collect.indices) {
//      val word = new WordNum()
//      word.setWord(collect(i)._1)
//      word.setNum(collect(i)._2)
//      seq = seq :+ word
//    }
//    result.setResult(seq.toList)
//    result
//  }
//}
