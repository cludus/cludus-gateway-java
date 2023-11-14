package xyz.cludus.gateway.services;

import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LocalSessionRegistry {
    private Map<String, UserSessionHandler> sessionsMap = new ConcurrentHashMap<>();

    @Autowired
    private GlobalSessionRegistry globalRegistry;

    public UserSessionHandler register(WebSocketSession session) {
        log.info("Registering a new websocket connection {}", UserSessionHandler.findUser(session));
        var result = new UserSessionHandler(session, this, globalRegistry);
        sessionsMap.put(result.getUser(), result);
        updateMetrics();
        return result;
    }

    public UserSessionHandler getSession(String user) {
        return sessionsMap.get(user);
    }

    public UserSessionHandler getSession(WebSocketSession session) {
        return getSession(UserSessionHandler.findUser(session));
    }

    public void sessionClosed(WebSocketSession session, CloseStatus status) {
        var userSession = getSession(session);
        if(session.isOpen()) {
            try {
                log.info("closing a websocket connection {}", UserSessionHandler.findUser(session));
                session.close(status);
                if(userSession != null) {
                    sessionsMap.remove(userSession.getUser());
                }
            }
            catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    public void evit() {
        sessionsMap.values().stream()
                .filter(UserSessionHandler::isIdle)
                .forEach(UserSessionHandler::closeSession);
        List<String> toRemove = sessionsMap.values().stream()
                .filter(x -> !x.isOpen())
                .map(UserSessionHandler::getUser)
                .toList();
        log.info("Evicting  {} websocket connections", toRemove.size());
        toRemove.forEach(k -> sessionsMap.remove(k));
        updateMetrics();
    }

    private void updateMetrics() {
        Metrics.gauge("cludus_gateway_connections_count", sessionsMap.size());
    }
}
