package com.hikvision.lohao.HRpc;

import org.apache.hadoop.ipc.ProtocolSignature;

import java.io.IOException;

/**
 * @author: lohao
 * @date: 2018/7/31
 * @description:
 */
public class ClientProtocolImpl implements ClientProtocol{

    @Override
    public String echo(String value) throws IOException {
        return value;
    }

    @Override
    public int add(int v1, int v2) throws IOException {
        return v1 + v2;
    }

    @Override
    public long getProtocolVersion(String s, long l) throws IOException {
        return ClientProtocol.versionID;
    }

    @Override
    public ProtocolSignature getProtocolSignature(String protocol, long clientVersion, int hashcode) throws IOException {
        return new ProtocolSignature(ClientProtocol.versionID, null);
    }
}