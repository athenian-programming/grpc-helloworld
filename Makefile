default: stubs bin

all: stubs bin

stubs: java-stubs python-stubs

bin: java-bin

java: java-stubs java-bin

python: python-stubs

dart: dart-stubs

clean:
	./gradlew clean

java-bin:
	./gradlew install

java-stubs:
	./gradlew assemble build

python-stubs:
	mkdir -p ./build/generated/source/python
	touch ./build/generated/source/python/__init__.py
	python3 -m grpc_tools.protoc -I. --python_out=./build/generated/source/python --grpc_python_out=./build/generated/source/python --proto_path=./src/main/proto helloworld.proto

dart-stubs:
	mkdir -p build/generated/source/dart/lib/src/generated
	protoc -I=src/main/proto --dart_out=grpc:build/generated/source/dart/lib/src/generated --proto_path=./src/main/proto helloworld.proto

java-client:
	build/install/HelloWorldGrpc/bin/helloworld-client

java-server:
	build/install/HelloWorldGrpc/bin/helloworld-server

python-client:
	python3 src/main/python/helloworld_client.py

python-server:
	python3 src/main/python/helloworld__server.py

# Maven targets
mvn-build:
	./mvnw -DskipTests=true clean package

mvn-clean:
	./mvnw -DskipTests=true clean

tree:
	./mvnw dependency:tree

jarcheck:
	./mvnw versions:display-dependency-updates

plugincheck:
	./mvnw versions:display-plugin-updates

versioncheck: jarcheck plugincheck
