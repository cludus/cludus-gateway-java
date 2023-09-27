package xyz.cludus.gateway;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CludusGatewayGrpcTest {
    @Autowired
    private Server server;

    @Test
    public void test() {
        var channel = ManagedChannelBuilder.forAddress("localhost", 9098)
                .usePlaintext()
                .build();
        var stub = HelloWorldServiceGrpc.newBlockingStub(channel);
        var result = stub.hello(HelloRequest.newBuilder().setText("Gilberto").build());
        System.out.println(result.getText());
    }
}
