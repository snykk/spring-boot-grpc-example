package com.najibfikri.grpcserver.server;

import com.najibfikri.grpc.ChatServiceGrpc;
import com.najibfikri.grpc.MessageRequest;
import com.najibfikri.grpc.MessageResponse;
import org.springframework.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

@GrpcService
public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    // Unary Communication
    @Override
    public void sendMessage(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
        System.out.println("Received: " + request.getMessage());
        System.out.println("From: " + request.getSender());

        MessageResponse response = MessageResponse.newBuilder()
                .setSender("Server")
                .setMessage("Received your message: " + request.getMessage())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Client Streaming - The server receives many messages, then returns a single response
    @Override
    public StreamObserver<MessageRequest> streamMessages(StreamObserver<MessageResponse> responseObserver) {
        return new StreamObserver<>() {
            StringBuilder combinedMessages = new StringBuilder();

            @Override
            public void onNext(MessageRequest request) {
                combinedMessages.append(request.getSender()).append(": ").append(request.getMessage()).append("\n");
                System.out.println("Client Streaming received: " + request.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                MessageResponse response = MessageResponse.newBuilder()
                        .setSender("Server")
                        .setMessage("All messages received:\n" + combinedMessages)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    // Server Streaming - The server sends multiple messages to the client in response to a single request
    @Override
    public void sendStreamedMessages(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
        System.out.println("Received: " + request.getMessage());
        System.out.println("From: " + request.getSender());

        // Simulate sending multiple messages over time
        for (int i = 0; i < 5; i++) {
            try {
                // Simulate some processing delay
                TimeUnit.SECONDS.sleep(1);

                MessageResponse response = MessageResponse.newBuilder()
                        .setSender("Server")
                        .setMessage("Message " + (i + 1) + ": " + request.getMessage())
                        .build();

                responseObserver.onNext(response);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                responseObserver.onError(e);
                return;
            }
        }

        // Complete the response stream
        responseObserver.onCompleted();
    }

    // Bidirectional Streaming - Server & Client exchange messages in real-time
    @Override
    public StreamObserver<MessageRequest> chatStream(StreamObserver<MessageResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(MessageRequest request) {
                System.out.println("Bi-directional Chat: " + request.getSender() + " says: " + request.getMessage());

                MessageResponse response = MessageResponse.newBuilder()
                        .setSender("Server")
                        .setMessage("Hello " + request.getSender() + ", you said: " + request.getMessage())
                        .build();

                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
