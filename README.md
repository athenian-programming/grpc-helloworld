# gRPC HelloWorld Example 

Uses: https://github.com/googleapis/gapic-generator-kotlin

## Java and Kotlin Setup

Generate the stubs with: `./gradlew generateProto`

Build the shell scripts with: `./gradlew installDist`

* ./build/install/grpc-helloworld/bin/java-client
* ./build/install/grpc-helloworld/bin/java-server
* ./build/install/grpc-helloworld/bin/kotlin-client
* ./build/install/grpc-helloworld/bin/kotlin-server

Build the uberjars with: `./gradlew -b build-uber.gradle java_server java_client kotlin_server kotlin_client`

* ./build/libs/java-server.jar
* ./build/libs/java-client.jar
* ./build/libs/kotlin-server.jar
* ./build/libs/kotlin-client.jar

## Python Setup

Generate the stubs with: `make python-stubs`



