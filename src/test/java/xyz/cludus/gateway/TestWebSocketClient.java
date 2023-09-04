package xyz.cludus.gateway;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class TestWebSocketClient {
    private List<String> messages = new ArrayList<>();
    private CountDownLatch latch;

    public TestWebSocketClient(CountDownLatch latch) {
        this.latch = latch;
    }

    @OnMessage
    public void onMessage(String message){
        messages.add(message);
        latch.countDown();
    }

    public List<String> getMessages() {
        return messages;
    }
}
