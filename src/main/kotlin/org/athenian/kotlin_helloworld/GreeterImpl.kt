package org.athenian.kotlin_helloworld

import io.grpc.stub.StreamObserver
import org.athenain.helloworld.GreeterGrpc
import org.athenain.helloworld.HelloReply
import org.athenain.helloworld.HelloRequest

class GreeterImpl : GreeterGrpc.GreeterImplBase() {

    override fun sayHello(request: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
        //val scope = HelloWorldServer.tracer.spanBuilder("$PREFIX.sayHello").startScopedSpan()!!
        val reply = HelloReply.newBuilder().apply { message = "Hello ${request.name}" }.build()
        responseObserver.apply {
            onNext(reply)
            //scope.close()
            onCompleted()
        }
    }

    override fun sayHelloWithManyRequests(responseObserver: StreamObserver<HelloReply>) =
            object : StreamObserver<HelloRequest> {
                val names: MutableList<String> = mutableListOf()
                //val scope = HelloWorldServer.tracer.spanBuilder("$PREFIX.sayHelloWithManyRequests").startScopedSpan()!!

                override fun onNext(request: HelloRequest) {
                    names.add(request.name)
                }

                override fun onError(t: Throwable) {
                    //scope.close()
                    println("Encountered error in sayHelloWithManyRequests()")
                    t.printStackTrace()
                }

                override fun onCompleted() {
                    val msg =
                            HelloReply.newBuilder()
                                    .apply { message = "Hello ${names.joinToString(", ")}" }
                                    .build()
                    responseObserver
                            .apply {
                                onNext(msg)
                                //scope.close()
                                onCompleted()
                            }
                }
            }

    override fun sayHelloWithManyReplies(request: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
        //val scope = HelloWorldServer.tracer.spanBuilder("$PREFIX.sayHelloWithManyReplies").startScopedSpan()!!
        repeat(5) {
            val reply =
                    HelloReply.newBuilder()
                            .apply { message = "Hello ${request.name} [$it]" }
                            .build()
            responseObserver.onNext(reply)
        }
        //scope.close()
        responseObserver.onCompleted()
    }

    override fun sayHelloWithManyRequestsAndReplies(responseObserver: StreamObserver<HelloReply>) =
            object : StreamObserver<HelloRequest> {
                //val scope = HelloWorldServer.tracer.spanBuilder("$PREFIX.sayHelloWithManyRequestsAndReplies").startScopedSpan()!!
                override fun onNext(request: HelloRequest) {
                    repeat(5) {
                        val reply =
                                HelloReply
                                        .newBuilder()
                                        .apply { message = "Hello ${request.name} [$it]" }
                                        .build()
                        responseObserver.onNext(reply)
                    }
                }

                override fun onError(t: Throwable) {
                    //scope.close()
                    println("Encountered error in sayHelloWithManyRequestsAndReplies()")
                    t.printStackTrace()
                }

                override fun onCompleted() {
                    //scope.close()
                    responseObserver.onCompleted()
                }
            }

    companion object {
        const val PREFIX = "grpc-helloworld.GreeterImpl"
    }
}
