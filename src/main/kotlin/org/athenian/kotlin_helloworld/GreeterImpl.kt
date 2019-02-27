package org.athenian.kotlin_helloworld

import io.grpc.stub.StreamObserver
import org.athenain.helloworld.GreeterGrpc
import org.athenain.helloworld.HelloReply
import org.athenain.helloworld.HelloRequest

class GreeterImpl : GreeterGrpc.GreeterImplBase() {

    override fun sayHello(request: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
        val reply = HelloReply.newBuilder()
                .setMessage("Hello ${request.name}")
                .build()
        responseObserver
                .apply {
                    onNext(reply)
                    onCompleted()
                }
    }

    override fun sayHelloWithManyRequests(responseObserver: StreamObserver<HelloReply>) =
            object : StreamObserver<HelloRequest> {
                val names: MutableList<String> = mutableListOf()

                override fun onNext(request: HelloRequest) {
                    names.add(request.name)
                }

                override fun onError(t: Throwable) {
                    println("Encountered error in sayHelloWithManyRequests()")
                    t.printStackTrace()
                }

                override fun onCompleted() {
                    val msg = HelloReply.newBuilder()
                            .setMessage("Hello ${names.joinToString(", ")}")
                            .build()
                    responseObserver
                            .apply {
                                onNext(msg)
                                onCompleted()
                            }
                }
            }

    override fun sayHelloWithManyReplies(request: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
        IntRange(0, 5)
                .forEach { i ->
                    val reply = HelloReply.newBuilder()
                            .setMessage("Hello ${request.name} [$i]")
                            .build()
                    responseObserver.onNext(reply)
                }

        responseObserver.onCompleted()
    }

    override fun sayHelloWithManyRequestsAndReplies(responseObserver: StreamObserver<HelloReply>): StreamObserver<HelloRequest> {
        return object : StreamObserver<HelloRequest> {
            override fun onNext(request: HelloRequest) {
                IntRange(0, 5)
                        .forEach { i ->
                            val reply = HelloReply.newBuilder()
                                    .setMessage("Hello ${request.name} [$i]")
                                    .build()
                            responseObserver.onNext(reply)
                        }
            }

            override fun onError(t: Throwable) {
                println("Encountered error in sayHelloWithManyRequestsAndReplies()")
                t.printStackTrace()
            }

            override fun onCompleted() {
                responseObserver.onCompleted()
            }
        }
    }
}
