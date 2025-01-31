package com.najibfikri.grpcclient.client;

import com.najibfikri.grpc.ChatServiceGrpc;
import com.najibfikri.grpc.MessageRequest;
import com.najibfikri.grpc.MessageResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class GrpcChatClientService {

    private final ManagedChannel channel;
    private final ChatServiceGrpc.ChatServiceBlockingStub chatServiceStub;
    private final ChatServiceGrpc.ChatServiceStub asyncChatServiceStub;

    public GrpcChatClientService(
            @Value("${grpc.server.host}") String host,
            @Value("${grpc.server.port}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        this.chatServiceStub = ChatServiceGrpc.newBlockingStub(channel);
        this.asyncChatServiceStub = ChatServiceGrpc.newStub(channel);
    }

    // Unary Communication - Send one message and receive a response
    public String sendMessage(String sender, String message) {
        MessageRequest request = MessageRequest.newBuilder()
                .setSender(sender)
                .setMessage(message)
                .build();

        MessageResponse response = chatServiceStub.sendMessage(request);
        return response.getMessage();
    }

    // Client Streaming - Send multiple messages and receive a single response
    public String sendMultipleMessages(String sender, String[] messages) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder finalResponse = new StringBuilder();

        StreamObserver<MessageRequest> requestObserver = asyncChatServiceStub.streamMessages(new StreamObserver<>() {
            @Override
            public void onNext(MessageResponse response) {
                finalResponse.append(response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        for (String msg : messages) {
            requestObserver.onNext(MessageRequest.newBuilder().setSender(sender).setMessage(msg).build());
        }
        requestObserver.onCompleted();

        latch.await(5, TimeUnit.SECONDS);
        return finalResponse.toString();
    }

    // Server Streaming - Send one message, and receive multiple
    public void receiveStreamedMessages(String sender, String message) {
        MessageRequest request = MessageRequest.newBuilder()
                .setSender(sender)
                .setMessage(message)
                .build();

        // Create an observer for handling the responses
        StreamObserver<MessageResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(MessageResponse response) {
                System.out.println("Received from server: " + response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream completed.");
            }
        };

        // Call the server streaming method using the async stub
        asyncChatServiceStub.sendStreamedMessages(request, responseObserver);
    }

    // Bidirectional Streaming - Real-time chat using Server-Sent Events (SSE)
    public void chatStream(String sender, SseEmitter emitter) {
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<MessageRequest> requestObserver = asyncChatServiceStub.chatStream(new StreamObserver<>() {
            @Override
            public void onNext(MessageResponse response) {
                try {
                    emitter.send(response.getMessage());
                } catch (Exception e) {
                    emitter.complete();
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                emitter.complete();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                emitter.complete();
                latch.countDown();
            }
        });

        // Send a few example messages
        requestObserver.onNext(MessageRequest.newBuilder().setSender(sender).setMessage("Hello Server").build());
        requestObserver.onNext(MessageRequest.newBuilder().setSender(sender).setMessage("How are you?").build());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        requestObserver.onCompleted();

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Shutdown the channel when the application stops
    public void shutdown() {
        channel.shutdown();
    }
}
