//package com.hikvision.lohao.demo;
//
//import com.hikvision.algorithmengine.algorithmtemplate.avroSchema.People;
//import com.hikvision.algorithmengine.algorithmtemplate.avroSchema.Peoples;
//import com.hikvision.algorithmengine.algorithmtemplate.template.AlgorithmTemplate;
//import org.apache.avro.specific.SpecificRecord;
//import org.apache.spark.sql.SparkSession;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author: lohao
// * @date: 2018/1/17
// * @description:
// */
//public class SleepDemo implements AlgorithmTemplate<SpecificRecord>{
//
//    @Override
//    public SpecificRecord run(SparkSession sc, String[] args) {
//        System.out.println("debug info:" + org.apache.http.conn.ssl.SSLConnectionSocketFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath());
//        Peoples peoples = new Peoples();
//
//        List<People> list = new ArrayList<People>();
//        for (int i = 0; i < 10; i++) {
//            People people = new People();
//            people.setAge(i);
//            people.setName("name" + i);
//            list.add(people);
//        }
//        try{
//            Thread.sleep(Integer.valueOf(args[0])*1000);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        peoples.setPersonArray(list);
//        return peoples;
//    }
//
//}