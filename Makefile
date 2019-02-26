default: stubs bin

all: stubs java-build bin

stubs: java-stubs python-stubs

bin: java-bin

java: java-stubs java-bin

python: python-stubs

dart: dart-stubs

java-build:
	./gradlew assemble build

clean:
	./gradlew clean

java-bin:
	./gradlew install

java-stubs:
	./gradlew generateProto

python-stubs:
	mkdir -p ./build/generated/source/python
	touch ./build/generated/source/python/__init__.py
	python3 -m grpc_tools.protoc -I. --python_out=./build/generated/source/python --grpc_python_out=./build/generated/source/python --proto_path=./src/main/proto helloworld.proto

dart-stubs:
	mkdir -p build/generated/source/dart/lib/src/generated
	protoc -I=src/main/proto --dart_out=grpc:build/generated/source/dart/lib/src/generated --proto_path=./src/main/proto helloworld.proto

java-client:
	build/install/HelloWorldGrpc/bin/java-helloworld-client

java-server:
	build/install/HelloWorldGrpc/bin/java-helloworld-server

kotlin-client:
	build/install/HelloWorldGrpc/bin/kotlin-helloworld-client

kotlin-server:
	build/install/HelloWorldGrpc/bin/kotlin-helloworld-server

python-client:
	python3 src/main/python/helloworld_client.py

python-server:
	python3 src/main/python/helloworld__server.py

versioncheck:
	./gradlew dependencyUpdates

