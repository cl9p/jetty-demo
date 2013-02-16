package com.cl9p.hazelcast.runnable;

import com.hazelcast.client.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

public class SampleHazelcastClient {
    public static void main(String args[]) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName("dev").setPassword("password");
        clientConfig.addAddress("localhost");



    }
}
