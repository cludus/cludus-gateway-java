package xyz.cludus.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class CludusGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(CludusGatewayApplication.class, args);
	}

}
