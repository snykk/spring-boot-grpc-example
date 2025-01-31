# General Variables
BASE_URL=http://localhost:8080/chat
HEADERS=-H "Content-Type: application/json"

# Unary Request (One message - one response)
unary:
	curl -X GET "$(BASE_URL)/send?sender=Bob&message=Hello" $(HEADERS)

# Client Streaming Request (Send multiple messages, receive one response)
client_stream:
	curl -X POST "$(BASE_URL)/client_stream?sender=Bob" \
		$(HEADERS) \
		-d '["Hello", "How are you?", "Goodbye"]'

# Server Streaming (One message, multiple responses)
server_stream:
	curl -X GET "$(BASE_URL)/server_stream?sender=Bob&message=Hello" $(HEADERS)

# Bidirectional Streaming (Real-time chat)
bidirectional:
	curl -X GET "$(BASE_URL)/bidirectional?sender=Bob" $(HEADERS)

# Run all requests
all: unary client_stream bidirectional server_stream

# Target to build and run gRPC Server
grpcserver:
	cd ./grpcserver && ./mvnw clean install && ./mvnw spring-boot:run

# Target to build and run gRPC Client
grpcclient:
	cd ./grpcclient && ./mvnw clean install && ./mvnw spring-boot:run

.PHONY: unary client_stream bidirectional server_stream all grpcserver grpcclient run_grpc
