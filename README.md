# gRPC HelloWorld Example 

## Gradle Instructions
Generate the `helloworld` stubs with:

```
$ ./gradlew assemble build
```

Build the `helloworld` apps with:

```
$ ./gradlew install
```

This creates the shell scripts:

* ./build/install/HelloWorldGrpc/bin/helloworld-client
* ./build/install/HelloWorldGrpc/bin/helloworld-server

## Maven Instructions
Generate the `helloworld` stubs with:

```
$ ./mvn clean protoc:compile protoc:compile-custom
```

Build the `helloworld` apps with:

```
$ ./mvn clean package
```

This creates the shell scripts: 

* ./target/bin/helloserver.sh
* ./target/bin/helloclient.sh

and the uber-jars:
 
* ./target/helloserver-1.0-jar-with-dependencies.jar
* ./target/helloclient-1.0-jar-with-dependencies.jar
 
Invoke the uber-jars with:

```
$ java -jar ./target/helloserver-1.0-jar-with-dependencies.jar
```

```
$ java -jar ./target/helloclient-1.0-jar-with-dependencies.jar
```

Build the docker images with:

```
$ ./bin/build-docker-images.sh
```

Run the server as a docker instance with:

```
$ docker run --name helloserver -p 50051:50051 -d cinch/helloserver:0.1.0
```

Determine the IP address of your docker machine with:

```
$ docker-machine env default | grep DOCKER_HOST
```

Connect to the server docker instance using the docker IP address with:

```
$ java -jar ./target/helloclient-1.0-jar-with-dependencies.jar --name test --times 10 --host 192.168.99.100
```


