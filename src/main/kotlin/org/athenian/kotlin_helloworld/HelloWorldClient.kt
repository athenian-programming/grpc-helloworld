package org.athenian.kotlin_helloworld

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.athenain.helloworld.GreeterGrpc
import org.athenain.helloworld.HelloReply
import org.athenain.helloworld.HelloRequest
import java.io.Closeable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HelloWorldClient internal constructor(private val channel: ManagedChannel) : Closeable {
    private val blockingStub: GreeterGrpc.GreeterBlockingStub = GreeterGrpc.newBlockingStub(channel)
    private val asyncStub: GreeterGrpc.GreeterStub = GreeterGrpc.newStub(channel)

    constructor(host: String, port: Int = 50051) :
            this(ManagedChannelBuilder.forAddress(host, port)
                         // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                         // needing certificates.
                         .usePlaintext()
                         .build())

    @Throws(InterruptedException::class)
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    override fun close() {
        shutdown()
    }

    fun sayHello(name: String) {
        val request = HelloRequest.newBuilder().setName(name).build()
        val response: HelloReply
        try {
            response = this.blockingStub.sayHello(request)
            println("sayHello() response: ${response.message}")
        } catch (e: StatusRuntimeException) {
            println("sayHello() failed: ${e.status}")
        }
    }

    fun sayHelloWithManyRequests(name: String) {
        val finishLatch = CountDownLatch(1)

        val responseObserver = object : StreamObserver<HelloReply> {
            override fun onNext(reply: HelloReply) {
                println("sayHelloWithManyRequests() response: ${reply.message}")
            }

            override fun onError(t: Throwable) {
                val status = Status.fromThrowable(t)
                println("sayHelloWithMayRequests() failed: $status")
                finishLatch.countDown()
            }

            override fun onCompleted() {
                finishLatch.countDown()
            }
        }

        val requestObserver = asyncStub.sayHelloWithManyRequests(responseObserver)

        try {
            for (i in 0..4) {
                val request = HelloRequest.newBuilder()
                        .setName("$name-$i")
                        .build()
                requestObserver.onNext(request)

                if (finishLatch.count == 0L) {
                    // RPC completed or errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    return
                }
            }
        } catch (e: RuntimeException) {
            // Cancel RPC
            requestObserver.onError(e)
            throw e
        }

        // Mark the end of requests
        requestObserver.onCompleted()

        // Receiving happens asynchronously
        try {
            finishLatch.await(1, TimeUnit.MINUTES)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun sayHelloWithManyReplies(name: String) {
        val request = HelloRequest.newBuilder().setName(name).build()
        val replies = blockingStub.sayHelloWithManyReplies(request)

        println("sayHelloWithManyReplies() responses:")
        replies.forEachRemaining { reply -> println(reply.message) }
        println()
    }

    fun sayHelloWithManyRequestsAndReplies(name: String) {
        val finishLatch = CountDownLatch(1)
        val responseObserver =
                object : StreamObserver<HelloReply> {
                    override fun onNext(reply: HelloReply) {
                        println("sayHelloWithManyRequestsAndReplies() response: ${reply.message}")
                    }

                    override fun onError(t: Throwable) {
                        val status = Status.fromThrowable(t)
                        println("sayHelloWithManyRequestsAndReplies() failed: $status")
                        finishLatch.countDown()
                    }

                    override fun onCompleted() {
                        finishLatch.countDown()
                    }
                }

        val requestObserver = asyncStub.sayHelloWithManyRequestsAndReplies(responseObserver)

        try {
            for (i in 0..4) {
                val request = HelloRequest.newBuilder().setName("$name-$i").build()
                println("sayHelloWithManyRequestsAndReplies() request: ${request.name}")
                requestObserver.onNext(request)

                if (finishLatch.count == 0L) {
                    // RPC completed or errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    return
                }
            }
        } catch (e: RuntimeException) {
            // Cancel RPC
            requestObserver.onError(e)
            throw e
        }

        // Mark the end of requests
        requestObserver.onCompleted()

        // Receiving happens asynchronously
        try {
            finishLatch.await(1, TimeUnit.MINUTES)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            HelloWorldClient("localhost")
                    .apply {
                        /* Access a service running on the local machine on port 50051 */
                        val name =
                                if (args.isNotEmpty())
                                    args[0] /* Use the arg as the name to greet if provided */
                                else
                                    "world"

                        sayHello(name)
                        sayHelloWithManyRequests(name)
                        sayHelloWithManyReplies(name)
                        sayHelloWithManyRequestsAndReplies(name)
                    }
        }
    }
}
