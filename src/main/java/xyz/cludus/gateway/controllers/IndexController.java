package xyz.cludus.gateway.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.cludus.gateway.dtos.ClientMessageDto;
import xyz.cludus.gateway.services.GlobalSessionRegistry;

@RestController
public class IndexController {
    @GetMapping("/")
    public String index() {
        return "Cludus Spring Gateway";
    }
}
