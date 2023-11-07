package xyz.cludus.gateway.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class GlobalSessionRegistry {
    @Autowired
    private StringRedisTemplate redis;

    private String gatewayId = UUID.randomUUID().toString();

    public String findGateway(String username) {
        return redis.opsForValue().get(username);
    }

    public void updateGateway(String user) {
        redis.opsForValue().set(user, gatewayId, Duration.ofMinutes(10));
    }
}
