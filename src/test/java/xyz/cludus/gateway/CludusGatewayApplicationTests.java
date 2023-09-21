package xyz.cludus.gateway;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import xyz.cludus.gateway.dtos.ClientMessageDto;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CludusGatewayApplicationTests {

	@LocalServerPort
	private int port;

	private static WebSocketContainer container;

	@BeforeAll
	public static void setup() {
		container = ContainerProvider.getWebSocketContainer();
	}

	@Test
	public void testGetLog() throws Exception {
		/*
		CountDownLatch cdl = new CountDownLatch(1);
		var client = new TestWebSocketClient(cdl);
		Session session = container.connectToServer(client,
				URI.create("ws://127.0.0.1:" + port + "/chat"));


		ClientMessageDto msg = new ClientMessageDto();
		msg.setAction(ClientMessageDto.Actions.SEND);

		session.getBasicRemote().sendText("hola");
		cdl.await();
        */
	}

}
