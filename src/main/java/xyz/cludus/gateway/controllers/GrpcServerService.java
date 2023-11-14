package xyz.cludus.gateway.controllers;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import xyz.cludus.gwcomm.*;

@GRpcService
public class GrpcServerService extends GatewayServiceGrpc.GatewayServiceImplBase {
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
        super.deliver(request, responseObserver);
    }
}
