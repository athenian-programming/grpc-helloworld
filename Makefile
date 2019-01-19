default: grpc

grpc:
	./gradlew assemble build
	python3 -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. ./proto/coordinate.proto

clean:
	./gradlew clean

bin:
	./gradlew install