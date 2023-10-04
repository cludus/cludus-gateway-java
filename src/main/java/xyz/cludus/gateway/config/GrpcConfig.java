package xyz.cludus.gateway.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GrpcConfig {
    @Bean
    public Server grpcServer() throws IOException {
        var server = ServerBuilder.forPort(9098)
                .addService(new HelloService())
                .build();
        server.start();
        return server;
    }
}
