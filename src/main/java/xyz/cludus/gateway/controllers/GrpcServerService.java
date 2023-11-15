package xyz.cludus.gateway.controllers;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import xyz.cludus.gateway.dtos.ServerMessageDto;
import xyz.cludus.gateway.services.LocalSessionRegistry;
import xyz.cludus.gwcomm.*;

@GRpcService
public class GrpcServerService extends GatewayServiceGrpc.GatewayServiceImplBase {
    @Autowired
    private LocalSessionRegistry localRegistry;

    @Override
    public void query(QueryRequest request, StreamObserver<QueryResponse> responseObserver) {
        final QueryResponse response = QueryResponse.newBuilder()
                                            .setResult(true)
                                            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deliver(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
        var reciptHandler = localRegistry.getSession(request.getRecipient());
        boolean result = false;
        if(reciptHandler != null) {
            reciptHandler.messageReceived(request);
            result = true;
        }
        final MessageResponse response = MessageResponse.newBuilder()
                .setResult(result)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
