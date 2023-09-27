package xyz.cludus.gateway.services;

import io.grpc.stub.StreamObserver;
import xyz.cludus.gateway.Hello;
import xyz.cludus.gateway.HelloRequest;
import xyz.cludus.gateway.HelloResponse;
import xyz.cludus.gateway.HelloWorldServiceGrpc;

public class HelloService extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

    @Override
    public void hello(
            HelloRequest request,
            StreamObserver<HelloResponse> responseObserver) {
        System.out.println(
                "Handling hello endpoint: " + request.toString());

        String text = request.getText() + " World";
        HelloResponse response =
                HelloResponse.newBuilder()
                        .setText(text).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}