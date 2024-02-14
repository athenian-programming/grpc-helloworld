default: versioncheck

all: stubs java-build bin

stubs: java-stubs python-stubs

bin: java-build

java: java-stubs java-build

python: python-stubs

java-build:
	./gradlew assemble build

clean:
	./gradlew clean

scripts:
	./gradlew installDist

jars:
	./gradlew -b build.gradle java_server java_client kotlin_server kotlin_client

app:
	./gradlew -b build-applications.gradle installDist

java-stubs:
	./gradlew generateProto

python-stubs:
	mkdir -p ./build/generated/source/python
	touch ./build/generated/source/python/__init__.py
	python3 -m grpc_tools.protoc -I. --python_out=./build/generated/source/python --grpc_python_out=./build/generated/source/python --proto_path=./src/main/proto helloworld.proto

java-client:
	build/install/grpc-helloworld/bin/java-helloworld-client

java-server:
	build/install/grpc-helloworld/bin/java-helloworld-server

kotlin-client:
	build/install/grpc-helloworld/bin/kotlin-helloworld-client

kotlin-server:
	build/install/grpc-helloworld/bin/kotlin-helloworld-server

python-client:
	python3 src/main/python/helloworld_client.py

python-server:
	python3 src/main/python/helloworld__server.py

refresh:
	./gradlew --refresh-dependencies dependencies

versioncheck:
	./gradlew dependencyUpdates

upgrade-wrapper:
	./gradlew wrapper --gradle-version=8.6 --distribution-type=bin