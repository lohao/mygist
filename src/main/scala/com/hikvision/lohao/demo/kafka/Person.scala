package com.hikvision.lohao.demo.kafka

case class Address(state: String, city: String) extends Serializable
case class Person(name: String, age: Long, gender: String, address: Address) extends Serializable
