package xyz.cludus.gateway.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.concurrent.atomic.AtomicLong;

public class ServerWebSocketHandler extends TextWebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ServerWebSocketHandler.class);

    private static final AtomicLong COUNT = new AtomicLong();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        COUNT.incrementAndGet();
        LOG.info("connection count: {}", COUNT.get());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String request = message.getPayload();
        String response = String.format("response from server to '%s'", HtmlUtils.htmlEscape(request));
        session.sendMessage(new TextMessage(response));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        COUNT.decrementAndGet();
        LOG.info("connection count: {}", COUNT.get());
    }
}
