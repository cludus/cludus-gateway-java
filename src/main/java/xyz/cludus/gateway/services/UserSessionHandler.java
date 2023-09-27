package xyz.cludus.gateway.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import xyz.cludus.gateway.dtos.ClientMessageDto;
import xyz.cludus.gateway.dtos.ServerMessageDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

public class UserSessionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UserSessionHandler.class);

    private static final Gson GSON = new Gson();

    private WebSocketSession session;
    @Getter
    private String user;
    private LocalDateTime lastUpdated;

    private UserSessionRegistry registry;

    public static String findUser(WebSocketSession session) {
        return Objects.requireNonNull(session.getPrincipal()).getName();
    }

    public UserSessionHandler(WebSocketSession session, UserSessionRegistry registry) {
        this.session = session;
        this.registry = registry;
        this.user = findUser(session);
        this.lastUpdated = LocalDateTime.now();
    }

    public boolean isIdle() {
        return session.isOpen() && lastUpdated.isBefore(LocalDateTime.now().minusMinutes(2));
    }

    public boolean isOpen() {
        return session.isOpen();
    }

    public void closeSession() {
        try {
            session.close(CloseStatus.NO_CLOSE_FRAME);
        }
        catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public void messageReceived(TextMessage message) {
        try {
            var clientMsg = readClientMessage(message);
            if(clientMsg.getAction() == ClientMessageDto.Actions.SEND) {
                sendActionReceived(clientMsg);
            }
            else if(clientMsg.getAction() == ClientMessageDto.Actions.HEARTBEAT) {
                heartBeatReceived(clientMsg);
            }
            var response = ServerMessageDto.ack();
            sendMessage(toTextMessage(response));
        }
        catch (JsonSyntaxException ex) {
            var response = ServerMessageDto.error(ex.getMessage());
            sendMessage(response);
        }
        catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            var response = ServerMessageDto.error(ex.getMessage());
            sendMessage(toTextMessage(response));
        }
    }

    void heartBeatReceived(ClientMessageDto clientMsg) {
        lastUpdated = LocalDateTime.now();
    }

    void sendActionReceived(ClientMessageDto clientMsg) throws IOException {
        lastUpdated = LocalDateTime.now();
        var response = ServerMessageDto.message(user, clientMsg.getContent());
        var reciptHandler = registry.getSession(clientMsg.getRecipient());
        if(reciptHandler != null) {
            reciptHandler.sendMessage(toTextMessage(response));
        }
        else {
            LOG.warn("User {} is not connected.", clientMsg.getRecipient());
        }
    }

    private ClientMessageDto readClientMessage(TextMessage message) {
        return GSON.fromJson(message.getPayload(), ClientMessageDto.class);
    }

    private TextMessage toTextMessage(ServerMessageDto message) {
        return new TextMessage(GSON.toJson(message));
    }

    synchronized void sendMessage(TextMessage message) {
        try {
            session.sendMessage(message);
        }
        catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    void sendMessage(ServerMessageDto message) {
        sendMessage(toTextMessage(message));
    }
}