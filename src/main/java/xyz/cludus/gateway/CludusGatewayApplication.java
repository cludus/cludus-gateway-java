package xyz.cludus.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.UUID;

@EnableDiscoveryClient
@SpringBootApplication
public class CludusGatewayApplication {

	public static void main(String[] args) {
		System.setProperty("INSTANCE_ID", UUID.randomUUID().toString().replace("-", ""));
		SpringApplication.run(CludusGatewayApplication.class, args);
	}

}
