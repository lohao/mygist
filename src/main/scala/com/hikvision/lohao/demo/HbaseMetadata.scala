package com.hikvision.lohao.demo

import org.apache.hadoop.hbase.{HBaseConfiguration, KeyValue, TableName}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.filter.PageFilter
import org.apache.hadoop.hbase.util.Bytes

object HbaseMetadata extends App {
  val config = HBaseConfiguration.create()
  config.set("hbase.zookeeper.quorum", "master66")
  config.set("hbase.zookeeper.property.clientPort", "2181")

  val hbaseConnection = ConnectionFactory.createConnection(config)
  val admin = hbaseConnection.getAdmin

  println("hbase namespace:")
  admin.listNamespaceDescriptors().foreach(x => println(x.getName))

  println("*" * 50)
  println("each hbase namespace tablename:")
  admin.listTableNames().map(x => getColumns(x, 100))



  def getColumns(hbaseTable: TableName, limitScan: Int): Set[String] = {
    val columnlist = Set[String]()

    val hTable = hbaseConnection.getTable(hbaseTable)
    val scan = new Scan()
    scan.setFilter(new PageFilter(limitScan))
    val results: ResultScanner = hTable.getScanner(scan)

    columnlist
  }


}
