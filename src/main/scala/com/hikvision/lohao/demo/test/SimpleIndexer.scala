package com.hikvision.lohao.demo.test

import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.ml.param.{Param, ParamMap, Params}
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

trait SimpleIndexerParam extends Params{
  final val inputCol = new Param[String](this, "inputCol", "The input column")
  final val outputCol = new Param[String](this, "outputCol", "The output column")

//  setDefault(inputCol, "label")
//  setDefault(outputCol, "indexedLabel")
}

class SimpleIndexer(override val uid: String) extends Estimator[SimpleIndexerModel] with SimpleIndexerParam {

  def setInputCol(value :String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def this() = this(Identifiable.randomUID("sampleIndexer"))

  override def copy(extra: ParamMap): SimpleIndexer = {
    defaultCopy(extra)
  }

  override def transformSchema(schema: StructType): StructType = {
    val idx = schema.fieldIndex($(inputCol))

    val field = schema.fields(idx)

    if(field.dataType != StringType) {
      throw new Exception(s"Input type ${field.dataType} did not match input type StringType")
    }

    schema.add(StructField($(outputCol), IntegerType, false))
  }

  override def fit(dataset: Dataset[_]): SimpleIndexerModel = {
    import dataset.sparkSession.implicits._
    val words = dataset.select(dataset($(inputCol))).as[String].distinct().collect()

    new  SimpleIndexerModel(uid, words)
  }
}


class SimpleIndexerModel(override val uid: String, val words: Array[String]) extends Model[SimpleIndexerModel] with SimpleIndexerParam {

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def copy(extra: ParamMap) = {
    defaultCopy(extra)
  }

  private  val lableToIndex: Map[String, Double] = words.zipWithIndex.map{case (x, y) => (x, y.toDouble)}.toMap

  import org.apache.spark.sql.functions._
  override def transform(dataset: Dataset[_]) = {
  val indexer = udf {label: String => lableToIndex(label)}

    dataset.select(col("*"), indexer(dataset(${inputCol}).cast(StringType)).as(${outputCol}))
  }

  override def transformSchema(schema: StructType) = {
    val idx = schema.fieldIndex($(inputCol))
    val field = schema.fields(idx)

    if (field.dataType != StringType) {
      throw new Exception(s"Input type ${field.dataType} did not match input type StringType")
    }
    // Add the return field
    schema.add(StructField($(outputCol), IntegerType, false))
  }
}
