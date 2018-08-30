package com.hikvision.lohao.HRpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

import java.io.IOException;

/**
 * @author: lohao
 * @date: 2018/7/31
 * @description:
 */
public class RServer {
    public static void main(String[] args) {
        Configuration conf = new Configuration();
        try {
            RPC.Server server = new RPC.Builder(conf).setProtocol(ClientProtocol.class).setInstance(new ClientProtocolImpl())
                    .setBindAddress("localhost").setPort(12345).setNumHandlers(2).build();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}