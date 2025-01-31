# spring-boot-grpc-example

This project demonstrates the integration of gRPC with Spring Boot, showcasing multiple communication patterns that can be used in gRPC services. It provides a simple chat service with the following gRPC RPC methods: Unary, Client Streaming, Bidirectional Streaming, and Server Streaming.

## Features

- **Unary Communication**: The client sends a single message and receives a response from the server.
- **Client Streaming**: The client sends multiple messages to the server, which then responds with a single message summarizing all received messages.
- **Server Streaming**: The client sends a single message, and the server responds with multiple messages over time.
- **Bidirectional Streaming**: Real-time two-way communication between the client and server, where both can send and receive messages simultaneously.
