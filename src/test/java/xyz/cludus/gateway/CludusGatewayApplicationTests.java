package xyz.cludus.gateway;

import com.redis.testcontainers.RedisContainer;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import xyz.cludus.gateway.services.JwtService;

import java.util.*;
import java.util.concurrent.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CludusGatewayApplicationTests {
	private static final Logger LOG = LoggerFactory.getLogger(CludusChatUser.class);

	@LocalServerPort
	private int port;

	@Autowired
	private JwtService jwtService;

	private static WebSocketContainer container;

	@Container
	private static final RedisContainer REDIS_CONTAINER =
			new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(6379);

	@Container
	public static final ConsulContainer CONSUL_CONTAINER =
			new ConsulContainer(DockerImageName.parse("hashicorp/consul:1.15")).withExposedPorts(8500);

	@DynamicPropertySource
	private static void registerRedisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
		registry.add("spring.data.redis.port", () -> {
			System.out.println(REDIS_CONTAINER.getMappedPort(6379).toString());
			return REDIS_CONTAINER.getMappedPort(6379).toString();
		});

		registry.add("spring.cloud.consul.host", CONSUL_CONTAINER::getHost);
		registry.add("spring.cloud.consul.port", () -> {
			System.out.println(CONSUL_CONTAINER.getMappedPort(8500).toString());
			return CONSUL_CONTAINER.getMappedPort(8500).toString();
		});
	}

	@Test
	void givenRedisContainerConfiguredWithDynamicProperties_whenCheckingRunningStatus_thenStatusIsRunning() {
		Assertions.assertTrue(REDIS_CONTAINER.isRunning());
	}

	@BeforeAll
	public static void setup() {
		container = ContainerProvider.getWebSocketContainer();
	}

	@Test
	public void testGetLog() throws Exception {
		Assertions.assertTrue(REDIS_CONTAINER.isRunning());
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
		Deque<CludusChatTestMessage> procesed = new ConcurrentLinkedDeque<>();
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
				LOG.error(ex.getMessage(), ex);
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
			users.put(userName, new CludusChatUser(container, port, userName, jwtService));
		}
		return users;
	}

}
