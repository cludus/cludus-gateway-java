package xyz.cludus.gateway;

import com.google.gson.Gson;
import jakarta.websocket.*;
import org.apache.tomcat.websocket.pojo.PojoMessageHandlerWholeText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.cludus.gateway.dtos.ClientMessageDto;
import xyz.cludus.gateway.dtos.ServerMessageDto;
import xyz.cludus.gateway.services.JwtService;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class CludusChatUser extends Endpoint {
    private static final Logger LOG = LoggerFactory.getLogger(CludusChatUser.class);

    private static final Gson GSON = new Gson();

    private WebSocketContainer container;
    private URI uri;
    private String user;
    private Map<String, CludusChatTestMessage> messages;
    private Session session;

    private JwtService jwtService;

    public CludusChatUser(WebSocketContainer container, int port, String user, JwtService jwtService) {
        this.container = container;
        this.uri = URI.create("ws://127.0.0.1:" + port + "/chat");
        this.user = user;
        this.jwtService = jwtService;
    }

    public void setMessages(Map<String, CludusChatTestMessage> messages) {
        this.messages = messages;
    }

    public void connect() throws DeploymentException, IOException {
        ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
        String jwt = jwtService.createToken(user);
        LOG.info("bearer token for user {}: {}", user, jwt);
        configBuilder.configurator(new ClientEndpointConfig.Configurator() {
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("Authorization", Arrays.asList("Bearer " + jwt));
            }
        });
        ClientEndpointConfig clientConfig = configBuilder.build();
        session = container.connectToServer(this, clientConfig, uri);
    }

    public void onMessage(String message) {
        var serverMsg = GSON.fromJson(message, ServerMessageDto.class);
        if(serverMsg.getAction() == ServerMessageDto.Actions.MESSAGE) {
            LOG.info("Message received " + serverMsg.getContent());
            var testMsg = messages.get(serverMsg.getContent());
            testMsg.setReceived(true);
            testMsg.setReceivedTs(System.currentTimeMillis());
        }
    }

    public void send(CludusChatTestMessage msg) throws DeploymentException, IOException {
        while (!session.isOpen()) {
            connect();
        }
        var clientMsg = new ClientMessageDto();
        clientMsg.setAction(ClientMessageDto.Actions.SEND);
        clientMsg.setContent(msg.getContent());
        clientMsg.setRecipient(msg.getTo());
        try {
            msg.setSent(true);
            msg.setSentTs(System.currentTimeMillis());
            session.getBasicRemote().sendText(GSON.toJson(clientMsg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                CludusChatUser.this.onMessage(message);
            }
        });
    }
}
