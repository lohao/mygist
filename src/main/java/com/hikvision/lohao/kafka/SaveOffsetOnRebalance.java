//package com.hikvision.lohao.kafka;
//
//import kafka.javaapi.consumer.ConsumerRebalanceListener;
//import org.apache.kafka.clients.consumer.Consumer;
//import org.apache.kafka.clients.consumer.OffsetAndMetadata;
//import org.apache.kafka.common.TopicPartition;
//
//import java.util.Collection;
//import java.util.Map;
//
///**
// * @author: lohao
// * @date: 2018/8/28
// * @description:

//onPartitionRevoked会在rebalance操作之前调用，用于我们提交消费者偏移，OnPartitionAssigned会在rebalance操作之后调用，用于我们拉取新的分配区的偏移量。
//
//        注意，OnPartitionAssigned我们需要的时重置本地拉取偏移量，所以我们拉取了服务端的消费偏移量初始化本地的拉取偏移量，拉取偏移量时本次拉取服务器消息的偏移量凭证，消费者偏移量时此分区已经消费消息的偏移量，在本地拉取偏移量未初始化的时候，我们一般会使用服务器端消费者偏移量重置（服务器的消费者偏移量可能是其他消费者在rebalance之前提交的）
//
//        第一次加入消费者组的时候，rebalance会执行，也就是启动本地consumer的时候，当有其他消费者加入的时候也会调用。
//
//
//        注意
//        1.拉取偏移量和消费偏移量的区别
//
//        2.调用时机
//
//        3.设置enable.auto.commit 为false，防止自动提交偏移量
// */
//class SaveOffsetOnRebalance implements ConsumerRebalanceListener {
//    private Consumer<String, String> consumer;
//
//    //初始化方法，传入consumer对象，否则无法调用外部的consumer对象，必须传入
//    public SaveOffsetOnRebalance(Consumer<String, String> consumer) {
//        this.consumer = consumer;
//    }
//
//    @Override
//    public void onPartitionsRevoked(Collection<TopicPartition> collection) {
//
//        //提交偏移量 主要是consumer.commitSync(toCommit); 方法
//        System.out.println("*- in ralance:onPartitionsRevoked");
//        //commitQueue 是我本地已消费消息的一个队列 是一个linkedblockingqueue对象
//        while (!commitQueue.isEmpty()) {
//            Map<TopicPartition, OffsetAndMetadata> toCommit = commitQueue.poll();
//            consumer.commitSync(toCommit);
//        }
//    }
//
//    @Override
//    public void onPartitionsAssigned(Collection<TopicPartition> collection) {
//        //rebalance之后 获取新的分区，获取最新的偏移量，设置拉取分量
//        System.out.println("*- in ralance:onPartitionsAssigned  ");
//        for (TopicPartition partition : collection) {
//            System.out.println("*- partition:"+partition.partition());
//
//            //获取消费偏移量，实现原理是向协调者发送获取请求
//            OffsetAndMetadata offset = consumer.committed(partition);
//            //设置本地拉取分量，下次拉取消息以这个偏移量为准
//            consumer.seek(partition, offset.offset());
//        }
//    }
//}
