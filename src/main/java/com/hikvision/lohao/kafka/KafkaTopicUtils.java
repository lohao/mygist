package com.hikvision.lohao.kafka;

import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.util.Properties;

/**
 * @author: lohao
 * @date: 2018/8/27
 * @description:
 */
public class KafkaTopicUtils {
    public static void createTopic(String newTopic) {
        ZkClient zkClient = null;
        ZkUtils zkUtils = null;
        try {
            String zookeeperHosts = "10.3.70.116:2181"; // If multiple zookeeper then -> String zookeeperHosts = "192.168.20.1:2181,192.168.20.2:2181";
            int sessionTimeOutInMs = 15 * 1000; // 15 secs
            int connectionTimeOutInMs = 10 * 1000; // 10 secs

            zkClient = new ZkClient(zookeeperHosts, sessionTimeOutInMs, connectionTimeOutInMs, ZKStringSerializer$.MODULE$);
            zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperHosts), false);

            int noOfPartitions = 2;
            int noOfReplication = 1;
            Properties topicConfiguration = new Properties();

            if (!AdminUtils.topicExists(zkUtils, newTopic)) {
                AdminUtils.createTopic(zkUtils, newTopic, noOfPartitions, noOfReplication, topicConfiguration);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
    }

}