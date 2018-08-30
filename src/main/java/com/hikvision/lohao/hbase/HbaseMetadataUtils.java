package com.hikvision.lohao.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * @author: lohao
 * @date: 2018/8/23
 * @description:
 */
public class HbaseMetadataUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(HbaseMetadataUtils.class);
    public Set<String> namespaces = new TreeSet<>();
    public Set<String> tableNamesWithNS = new TreeSet<>();
    public Map<String, Set<String>> tableColumnsMap = new HashMap<>();

    public static Configuration config;

    static {
        config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "10.3.70.122,10.3.70.122,10.3.70.124");
        config.setInt("hbase.zookeeper.property.clientPort", 2181);
        config.setLong("hbase.rpc.timeout", 10000);
        config.setInt("hbase.client.retries.number", 3);
        config.setLong("zookeeper.session.timeout", 30000);
        config.setLong("hbase.htable.threads.keepalivetime", 3600);
//        config.set("hbase.master", "master66:6000");
    }

    public void test() {
        try {
            Connection hbaseConnectiion = ConnectionFactory.createConnection(config);

            Admin admin = hbaseConnectiion.getAdmin();
            System.out.println("hbase contains some databases:");
            NamespaceDescriptor[] namespaceDescriptors = admin.listNamespaceDescriptors();
            for(NamespaceDescriptor namespaceDescriptor: namespaceDescriptors) {
                if(namespaceDescriptor.getName().equals("hbase"))
                    continue;
                namespaces.add(namespaceDescriptor.getName());
                TableName[] tableNames = admin.listTableNamesByNamespace(namespaceDescriptor.getName());
                for(TableName tableName: tableNames) {
                   tableNamesWithNS.add(tableName.getNameWithNamespaceInclAsString());

                    Table table = hbaseConnectiion.getTable(TableName.valueOf(tableName.getNamespace(), tableName.getQualifier()));
                    System.out.println(tableName.getNamespaceAsString() + ":" + tableName.getQualifierAsString());

                    ResultScanner resultScanner = table.getScanner(new Scan());

                    for(Result result: resultScanner) {
                        NavigableMap<byte[],NavigableMap<byte[], byte[]>> allInfos = result.getNoVersionMap();
                        allInfos.entrySet().forEach(info -> {
                            Set<String> columnsWithPrefix = new TreeSet<>();
                            final String columnPrefix = Bytes.toString(info.getKey());
                            info.getValue().keySet().forEach(column -> {
                                String columnWithPrefix = columnPrefix +":" + Bytes.toString(column);
                                columnsWithPrefix.add(columnWithPrefix);
                            });
                            tableColumnsMap.putIfAbsent(tableName.getNameWithNamespaceInclAsString(), columnsWithPrefix);
                        });
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new HbaseMetadataUtils().test();
    }
}