package xyz.cludus.gateway;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import xyz.cludus.gateway.dtos.ClientMessageDto;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CludusGatewayApplicationTests {
	private static final Logger LOG = LoggerFactory.getLogger(CludusChatUser.class);

	@LocalServerPort
	private int port;

	private static WebSocketContainer container;

	@BeforeAll
	public static void setup() {
		container = ContainerProvider.getWebSocketContainer();
	}

	@Test
	public void testGetLog() throws Exception {
		Map<String, CludusChatUser> users = createUsers(10);
		LinkedList<CludusChatTestMessage> messages = createMessages(10000, users);
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
			try {
				LOG.info("sending message, {} remaining", messages.size());
				if (messages.isEmpty()) {
					return;
				}
				var toSend = messages.removeFirst();
				users.get(toSend.getFrom()).send(toSend);
				procesed.add(toSend);
				if (messages.isEmpty()) {
					cdl.countDown();
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}, 1000, 1, TimeUnit.MILLISECONDS);
		cdl.await();

		//Should have received all messages
		Assertions.assertEquals(0, messages.stream().filter(x -> !x.isReceived()).count() );
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
