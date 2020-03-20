# gRPC HelloWorld Example 

Uses: https://github.com/googleapis/gapic-generator-kotlin

## Java and Kotlin Setup

Generate the stubs with: `./gradlew generateProto`

Build the shell scripts with: 
```shell script
./gradlew installDist`
```

Which builds:

* ./build/install/grpc-helloworld/bin/java-client
* ./build/install/grpc-helloworld/bin/java-server
* ./build/install/grpc-helloworld/bin/kotlin-client
* ./build/install/grpc-helloworld/bin/kotlin-server

Build the uberjars with: 
```shell script
./gradlew -b build-uber.gradle java_server java_client kotlin_server kotlin_client
```

Which builds:

* ./build/libs/java-server.jar
* ./build/libs/java-client.jar
* ./build/libs/kotlin-server.jar
* ./build/libs/kotlin-client.jar

## Python Setup

Generate the stubs with: `make python-stubs`



