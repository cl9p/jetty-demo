package com.cl9p.websocket.servlet;

import com.cl9p.websocket.service.ChatService;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class ChatWebSocketServlet extends WebSocketServlet {
    Logger logger = Logger.getLogger(ChatWebSocketServlet.class);

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        logger.info("== configure servlet ==");
        webSocketServletFactory.register(ChatService.class);
    }
}
