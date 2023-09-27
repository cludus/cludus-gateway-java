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
import java.util.*;
import java.util.concurrent.*;

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
		Map<String, CludusChatUser> users = createUsers(100);
		LinkedList<CludusChatTestMessage> messages = createMessages(100000, users);
		Map<String, CludusChatTestMessage> messageByContent = new ConcurrentHashMap<>();
		messages.forEach(x -> {
			messageByContent.put(x.getContent(), x);
		});

		users.forEach((k, v) -> {
			try {
				v.setMessages(messageByContent);
				v.connect();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});

		CountDownLatch cdl = new CountDownLatch(1);
		LinkedList<CludusChatTestMessage> procesed = new LinkedList<>();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
		executor.scheduleAtFixedRate(() -> {
			var toSend = messages.removeFirst();
			users.get(toSend.getFrom()).send(toSend);
			procesed.add(toSend);
			if(messages.isEmpty()) {
				cdl.countDown();
			}
		}, 10, 10, TimeUnit.MILLISECONDS);
		cdl.await();

		//TODO verify
	}

	private LinkedList<CludusChatTestMessage> createMessages(int count, Map<String, CludusChatUser> users) {
		Random random = new Random();
		List<String> userList = new ArrayList<>(users.keySet());
		LinkedList<CludusChatTestMessage> list = new LinkedList<>();
		for(int i = 0; i < count; i++) {
			String from = userList.get(random.nextInt(userList.size()));
			String to = userList.get(random.nextInt(userList.size()));
			String content = UUID.randomUUID().toString();
			list.add(new CludusChatTestMessage(from, to, content));
		}
		return list;
	}

	private Map<String, CludusChatUser> createUsers(int count) {
		Map<String, CludusChatUser> users = new HashMap<>();
		for(int i = 0; i < count; i++) {
			String userName = "user" + i;
			users.put(userName, new CludusChatUser(container, port, userName));
		}
		return users;
	}

}
