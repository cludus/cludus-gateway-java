package xyz.cludus.gateway.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.HtmlUtils;

public class ServerWebSocketHandler extends TextWebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ServerWebSocketHandler.class);

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String request = message.getPayload();
        LOG.info("Server received: {}", request);

        String response = String.format("response from server to '%s'", HtmlUtils.htmlEscape(request));
        LOG.info("Server sends: {}", response);
        session.sendMessage(new TextMessage(response));
    }
}
