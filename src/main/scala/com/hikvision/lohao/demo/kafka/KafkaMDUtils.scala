package com.hikvision.lohao.demo.kafka

import java.util.Properties
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConverters._
import com.google.gson.Gson
import com.hikvision.lohao.kafka.KafkaTopicUtils
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}
import org.codehaus.jackson.map.ObjectMapper

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

object KafkaMDUtils {

  import scala.concurrent.ExecutionContext.Implicits.global

  final val newTopic = "lohao"
  final val promise = Promise[RecordMetadata]


  def createTopic(): Unit = {
    KafkaTopicUtils.createTopic(newTopic)
  }

  def producerForTopic(): Unit = {
    val brokerList = "10.3.70.116:9092"
    val props = new Properties()
    props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList)
    props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer].getName)
    props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000")
    props.setProperty(ProducerConfig.ACKS_CONFIG, "all")
    props.setProperty(ProducerConfig.RETRIES_CONFIG, "0")

    val gson = new Gson()
    val producer = new KafkaProducer[Array[Byte], String](props)
    var count = 0L
    while (true) {
      count += 1
      Thread.sleep(1000)
      val value = gson.toJson(Person("lucy", count, "female", Address("shandong", "qingdao")))
      sendToKafka(newTopic, "person".getBytes, value)(producer)
    }

    //    val record = new ProducerRecord[Array[Byte], String](newTopic, "person".getBytes, json)
    //    producer.send(record, new ProducerCallback)
    producer.close()

  }

  class ProducerCallback extends Callback {
    override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
      if (exception != null) {
        println(s"error occurs when send message to kafka.")
        exception.printStackTrace()
      } else {
        println(s"Successfully send message: ${metadata}")
      }
    }
  }


  def sendToKafka(topic: String, key: Array[Byte], value: String)(producer: KafkaProducer[Array[Byte], String]): Unit = {
    producer.send(new ProducerRecord(topic, null, value), producerCallback(promise))

    //for promise callback
    promise.future.onComplete {
      case Success(recordMetadata) => println(s"Thread ${Thread.currentThread().getName} Successfully send message. " +
        s"offset:${recordMetadata.offset()} partition:${recordMetadata.partition()}")
      case Failure(exception) => println(exception.getMessage)
    }
  }


  private def producerCallback(promise: Promise[RecordMetadata]): Callback = {
    new Callback {
      override def onCompletion(recordMetadata: RecordMetadata, e: Exception): Unit = {
        if (e == null) {
          println(s"Successfully send message." +
            s" offset:${recordMetadata.offset()} partition:${recordMetadata.partition()}")
        } else {
          println(s"error occurs when send message to kafka.")
          e.printStackTrace()
        }

        //for promise for promise's future
        //        val result = if(e == null) Success(recordMetadata) else Failure(e)
        //        promise.complete(result)
      }
    }
  }

  def consumerForTopicAuto(): Unit = {
    val bootstrapServer = "10.3.70.116:9092"
    val properties = new Properties()
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[ByteArrayDeserializer].getName)
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "lohao")
    //此参数配置的意思是有全新的group.id加入的时候，从头开始，否则不起作用
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val consumer = new KafkaConsumer[String, String](properties)
    //1. 由于subscribe是lazy的，需要使用poll将consumer和topic联系起来，然后才能重置offset，重置offset的过程可能会有rebalance的过程
    //rebalance是异步的过程，需要一段时间，所以可能隔一段时候offset才可以重置，重新消费
    //2. assign函数是针对一个分区，所以可以直接建立连接，也可以直接重置offset，不会引起rebalance的过程。
    consumer.assign(List(new TopicPartition(newTopic, 1)).asJava)
//    consumer.poll(100)
    consumer.seekToBeginning(new TopicPartition(newTopic, 1))

    var count = 0
    while (true) {
      count += 1
      val records: ConsumerRecords[String, String]  = consumer.poll(1000)
      val recordIterator = records.iterator()
      while (recordIterator.hasNext) {
        val record = recordIterator.next()
        println(s"count = ${count}, offset = ${record.offset()}, key = ${record.key()}, value = ${record.value()}")
      }
    }

    consumer.close()
  }

  def main(args: Array[String]): Unit = {
    createTopic()
    //    producerForTopic()
    consumerForTopicAuto()
  }
}
