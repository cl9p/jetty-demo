package com.cl9p.websocket.service;

import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.FutureCallback;
import org.eclipse.jetty.websocket.api.WebSocketConnection;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@WebSocket(maxTextSize = 64 * 1024)
public class ChatService {
    Logger logger = Logger.getLogger(ChatService.class);
    QueueChecker queueChecker;


    public ChatService() {
    }


    @OnWebSocketMessage
    public void onText(WebSocketConnection connection, String message) {
        if (connection.isOpen()) {
            return;
        } else {
            connection.write(message);
        }
    }

    @OnWebSocketConnect
    public void onConnect(WebSocketConnection connection) {
        logger.info("== WebSocket Connected ==");
        queueChecker = new QueueChecker(connection);
        queueChecker.start();
    }

    @OnWebSocketClose
    public void onClose(WebSocketConnection connection, int status, String message) throws IOException {
        logger.info("== WebSocket Disconnected ==");
        if (connection.isOpen()) {
            queueChecker.client.getLifecycleService().shutdown();
        }
    }

    private class QueueChecker extends Thread {
        Logger logger = Logger.getLogger(QueueChecker.class);
        WebSocketConnection connection;
        ClientConfig clientConfig = new ClientConfig();
        HazelcastInstance client;
        Queue<String> queue;

        private QueueChecker(WebSocketConnection connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            clientConfig.getGroupConfig().setName("dev").setPassword("password");
            clientConfig.addAddress("localhost");
            client = HazelcastClient.newHazelcastClient(clientConfig);
            queue = client.getQueue("chat");
            while (client.getLifecycleService().isRunning()) {
                try {
                    if (!queue.isEmpty()) {
                        String task = queue.remove();
                        logger.info("task == " + task);
                        connection.write(task);
                    }
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}
