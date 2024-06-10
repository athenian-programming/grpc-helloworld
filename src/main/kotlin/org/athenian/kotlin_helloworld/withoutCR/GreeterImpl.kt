package org.athenian.kotlin_helloworld.withoutCR

import io.grpc.stub.StreamObserver
import org.athenian.helloworld.GreeterGrpc
import org.athenian.helloworld.HelloReply
import org.athenian.helloworld.HelloRequest

class GreeterImpl : GreeterGrpc.GreeterImplBase() {

    override fun sayHello(request: HelloRequest, responseObserver: StreamObserver<HelloReply>) =
        with(responseObserver) {
            val reply =
                HelloReply.newBuilder()
                    .run {
                        message = "Hello ${request.name}"
                        build()
                    }
            onNext(reply)
            onCompleted()
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

            override fun onCompleted() =
                with(responseObserver) {
                    val msg =
                        HelloReply.newBuilder()
                            .run {
                                message = "Hello ${names.joinToString(", ")}"
                                build()
                            }
                    onNext(msg)
                    onCompleted()
                }
        }

    override fun sayHelloWithManyReplies(request: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
        repeat(5) {
            val reply =
                HelloReply.newBuilder()
                    .run {
                        message = "Hello ${request.name} [$it]"
                        build()
                    }
            responseObserver.onNext(reply)
        }
        responseObserver.onCompleted()
    }

    override fun sayHelloWithManyRequestsAndReplies(responseObserver: StreamObserver<HelloReply>) =
        object : StreamObserver<HelloRequest> {
            override fun onNext(request: HelloRequest) {
                repeat(5) {
                    val reply =
                        HelloReply.newBuilder()
                            .run {
                                message = "Hello ${request.name} [$it]"
                                build()
                            }
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
