default: grpc

grpc:
	./gradlew assemble build

clean:
	./gradlew clean

bin:
	./gradlew install