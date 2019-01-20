default: grpc

grpc:
	./gradlew assemble build
	mkdir -p ./build/generated/source/python
#	python3 -m grpc_tools.protoc -I. --python_out=./build/generated/source/python --grpc_python_out=./build/generated/source/python --proto_path=./src/main/proto helloworld.proto
	python3 -m grpc_tools.protoc -I. --python_out=./src/main/python --grpc_python_out=./src/main/python --proto_path=./src/main/proto helloworld.proto
	touch ./build/generated/source/python/__init__.py

clean:
	./gradlew clean

bin:
	./gradlew install

java-client:
	build/install/HelloWorldGrpc/bin/hello-world-client

java-server:
	build/install/HelloWorldGrpc/bin/hello-world-server

python-client:
	python3 src/main/python/greeter_client.py

python-server:
	python src/main/python/greeter_server.py
