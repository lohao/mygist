package com.hikvision.lohao.HRpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author: lohao
 * @date: 2018/7/31
 * @description:
 */
public class RClient {
    public static void main(String[] args) {
        Configuration conf = new Configuration();
        try {
            ClientProtocol proxy = (ClientProtocol)RPC.getProxy(ClientProtocol.class, ClientProtocol.versionID, new InetSocketAddress("localhost", 12345), conf);
            System.out.println(proxy.add(1,2));
            System.out.println(proxy.echo("hello"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}