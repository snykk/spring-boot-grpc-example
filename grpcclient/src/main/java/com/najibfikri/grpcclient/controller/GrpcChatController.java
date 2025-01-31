package com.najibfikri.grpcclient.controller;

import com.najibfikri.grpcclient.client.GrpcChatClientService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class GrpcChatController {
    private final GrpcChatClientService grpcChatClientService;

    public GrpcChatController(GrpcChatClientService grpcChatClientService) {
        this.grpcChatClientService = grpcChatClientService;
    }

    // Unary Request - Send one message, receive one response
    @GetMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String sender, @RequestParam String message) {
        String response = grpcChatClientService.sendMessage(sender, message);
        return ResponseEntity.ok(response);
    }

    // Client Streaming - Send multiple messages, receive one response
    @PostMapping("/client_stream")
    public ResponseEntity<String> sendMultipleMessages(@RequestParam String sender, @RequestBody List<String> messages) throws InterruptedException {
        String response = grpcChatClientService.sendMultipleMessages(sender, messages.toArray(new String[0]));
        return ResponseEntity.ok(response);
    }

    // Server Streaming - Receive multiple responses to a single request
    @GetMapping("/server_stream")
    public ResponseEntity<Void> receiveStreamedMessages(@RequestParam String sender, @RequestParam String message) {
        grpcChatClientService.receiveStreamedMessages(sender, message);
        return ResponseEntity.ok().build();
    }

    // Bidirectional Streaming - Real-time chat using SSE
    @GetMapping(value = "/bidirectional", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam String sender) {
        SseEmitter emitter = new SseEmitter();
        grpcChatClientService.chatStream(sender, emitter);
        return emitter;
    }
}
