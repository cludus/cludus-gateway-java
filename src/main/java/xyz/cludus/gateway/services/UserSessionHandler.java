package xyz.cludus.gateway.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import xyz.cludus.gateway.dtos.ClientMessageDto;
import xyz.cludus.gateway.dtos.ServerMessageDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
public class UserSessionHandler {
    private static final Gson GSON = new Gson();

    private WebSocketSession session;

    @Getter
    private String user;
    private LocalDateTime lastUpdated;

    private LocalSessionRegistry localRegistry;

    private GlobalSessionRegistry globalRegistry;

    public static String findUser(WebSocketSession session) {
        return Objects.requireNonNull(session.getPrincipal()).getName();
    }

    public UserSessionHandler(WebSocketSession session, LocalSessionRegistry localRegistry, GlobalSessionRegistry globalRegistry) {
        this.session = session;
        this.localRegistry = localRegistry;
        this.globalRegistry = globalRegistry;
        this.user = findUser(session);
        this.lastUpdated = LocalDateTime.now();
        globalRegistry.updateGateway(user);
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
            log.error(ex.getMessage(), ex);
        }
    }

    public void messageReceived(TextMessage message) {
        try {
            if(log.isDebugEnabled()) {
                log.debug("Message received: {}, from user {}", GSON.toJson(message), user);
            }
            var clientMsg = readClientMessage(message);
            if(clientMsg.getAction() == ClientMessageDto.Actions.SEND) {
                sendActionReceived(clientMsg);
            }
            else if(clientMsg.getAction() == ClientMessageDto.Actions.HEARTBEAT) {
                heartBeatReceived(clientMsg);
            }

            if(log.isDebugEnabled()) {
                log.debug("Sending ack response for message: {} to {}", GSON.toJson(message), user);
            }
            var response = ServerMessageDto.ack();
            sendMessage(toTextMessage(response));
        }
        catch (JsonSyntaxException ex) {
            var response = ServerMessageDto.error(ex.getMessage());
            sendMessage(response);
        }
        catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            var response = ServerMessageDto.error(ex.getMessage());
            sendMessage(toTextMessage(response));
        }
    }

    void heartBeatReceived(ClientMessageDto clientMsg) {
        lastUpdated = LocalDateTime.now();
        globalRegistry.updateGateway(user);
    }

    public void messageReceived(ServerMessageDto msg) {
        if(log.isDebugEnabled()) {
            log.debug("Sending message to client: {} from user {}", GSON.toJson(msg), user);
        }
        sendMessage(toTextMessage(msg));
    }

    void sendActionReceived(ClientMessageDto clientMsg) throws IOException {
        lastUpdated = LocalDateTime.now();
        var response = ServerMessageDto.message(user, clientMsg.getRecipient(), clientMsg.getContent());
        var reciptHandler = localRegistry.getSession(clientMsg.getRecipient());
        if(reciptHandler != null) {
            reciptHandler.messageReceived(response);
        }
        else {
            String userGw = globalRegistry.findGateway(response.getRecipient());
            if(userGw != null) {
                log.info("User {} is connected to gateway {}.", response.getRecipient(), userGw);
                globalRegistry.sendMessage(userGw, response);
            }
            else {
                log.info("User {} is not connected.", clientMsg.getRecipient());
            }
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
            log.error(ex.getMessage(), ex);
        }
    }

    void sendMessage(ServerMessageDto message) {
        sendMessage(toTextMessage(message));
    }
}
