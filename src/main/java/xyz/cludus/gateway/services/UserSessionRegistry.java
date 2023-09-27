package xyz.cludus.gateway.services;

import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(UserSessionRegistry.class);
    private Map<String, UserSessionHandler> sessionsMap = new ConcurrentHashMap<>();

    public UserSessionHandler register(WebSocketSession session) {
        var result = new UserSessionHandler(session, this);
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
                session.close(status);
                sessionsMap.remove(userSession.getUser());
            }
            catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
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
        toRemove.forEach(k -> sessionsMap.remove(k));
        updateMetrics();
    }

    private void updateMetrics() {
        Metrics.gauge("cludus_gateway_connections_count", sessionsMap.size());
    }
}
