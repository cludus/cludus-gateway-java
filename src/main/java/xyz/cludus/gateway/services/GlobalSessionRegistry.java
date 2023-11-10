package xyz.cludus.gateway.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xyz.cludus.gateway.dtos.ClientMessageDto;
import xyz.cludus.gateway.dtos.ServerMessageDto;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Service
public class GlobalSessionRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalSessionRegistry.class);

    @Autowired
    private StringRedisTemplate redis;

    @Value("${spring.cloud.consul.discovery.instanceId}")
    private String gatewayId;

    @Autowired
    private RestTemplate rest;

    @Autowired
    private DiscoveryClient discoveryClient;

    public String findGateway(String username) {
        return redis.opsForValue().get(username);
    }

    public void updateGateway(String user) {
        LOG.info("Registering user: {} on gateway instance: {}", user, gatewayId);
        redis.opsForValue().set(user, gatewayId, Duration.ofMinutes(10));
    }

    public void sendMessage(String gatewayId, ServerMessageDto message) {
        URI uri = findUri(gatewayId);
        if(uri != null) {
            rest.postForObject(uri + "/send-message", message, String.class);
        }
    }

    private URI findUri(String gatewayId) {
        var instances =  discoveryClient.getInstances("cludus-gateway");
        for (var instance : instances) {
            if(instance.getInstanceId().equals(gatewayId)) {
                System.out.println(instance.getUri());
                return instance.getUri();
            }
        }
        return null;
    }
}
