//package com.hikvision.lohao;
//
//import com.aliyun.oss.OSSClient;
//import com.aliyun.oss.model.OSSObjectSummary;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
///**
// * @author: lohao
// * @date: 2018/1/29
// * @description:
// */
//public class ObjectList {
//    public static void main(String[] args) {
//        if(args.length == 0) {
//            System.out.println("usage: java -jar object-list-tools.jar bucketName endPoint accessKey secretAccesskey");
//            System.exit(1);
//        }
//        String bucketName = args[0];
//        String endPoint = args[1];
//        String accessKey = args[2];
//        String secretAccessKey = args[3];
//        OSSClient objectClient = new OSSClient(bucketName + "." + endPoint, accessKey, secretAccessKey);
//
//
//        List<String> list = new ArrayList<>();
//        list.add(String.format("%-15s   %-20s   %-18s   %-10s", "bucketName", "objectName", "laseModifideTime", "size"));
//        for (OSSObjectSummary summary : objectClient.listObjects(bucketName).getObjectSummaries()) {
//            list.add(String.format("%-15s : %-20s : %-18s : %-10s", summary.getBucketName(), summary.getKey(), summary.getLastModified().toLocaleString(), summary.getSize()));
//        }
//
//        Collections.sort(list);
//
//        for (String item : list) {
//            System.out.println(item);
//        }
//    }
//}