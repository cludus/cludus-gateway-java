package xyz.cludus.gateway.controllers;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xyz.cludus.gateway.dtos.ClientMessageDto;
import xyz.cludus.gateway.dtos.ServerMessageDto;
import xyz.cludus.gateway.services.GlobalSessionRegistry;
import xyz.cludus.gateway.services.LocalSessionRegistry;

@RestController
@Slf4j
public class IncomingChatController {
    @Autowired
    private LocalSessionRegistry localRegistry;

    @PostMapping("/send-message")
    public String sendMessage(@RequestBody ServerMessageDto msg) {
        var reciptHandler = localRegistry.getSession(msg.getRecipient());
        if(reciptHandler != null) {
            reciptHandler.messageReceived(msg);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "recipient not found");
        }
        return "OK";
    }
}
