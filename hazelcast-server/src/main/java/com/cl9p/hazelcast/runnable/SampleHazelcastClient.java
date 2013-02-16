package com.cl9p.hazelcast.runnable;

import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class SampleHazelcastClient {
    public static void main(String args[]) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName("dev").setPassword("password");
        clientConfig.addAddress("localhost");

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        Queue<String> q = client.getQueue("chat");
        q.offer("Hello World!");
        client.shutdown();

    }
}
