package org.athenian.kotlin_helloworld

import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.opencensus.contrib.grpc.metrics.RpcViews
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector
import io.prometheus.client.exporter.HTTPServer
import org.athenain.helloworld.GreeterGrpc
import org.athenain.helloworld.HelloReply
import org.athenain.helloworld.HelloRequest
import java.io.Closeable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class HelloWorldClientNoCR internal constructor(private val channel: ManagedChannel) : Closeable {
    private val blockingStub: GreeterGrpc.GreeterBlockingStub = GreeterGrpc.newBlockingStub(channel)
    private val asyncStub: GreeterGrpc.GreeterStub = GreeterGrpc.newStub(channel)

    constructor(host: String, port: Int = 50051) :
            this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build())

    init {
        channel.apply {
            notifyWhenStateChanged(ConnectivityState.CONNECTING) { println("Connecting: ${getState(false)}") }
            notifyWhenStateChanged(ConnectivityState.READY) { println("Ready: ${getState(false)}") }
            notifyWhenStateChanged(ConnectivityState.IDLE) { println("Idle: ${getState(false)}") }
        }
    }

    fun sayHello(name: String) {
        val request = HelloRequest.newBuilder().setName(name).build()
        val response = blockingStub.sayHello(request)
        println("sayHello() response: ${response.message}")
    }

    fun sayHelloWithManyRequests(name: String) {
        val finishLatch = CountDownLatch(1)
        val responseObserver =
                object : StreamObserver<HelloReply> {
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
            repeat(5) {
                val request = HelloRequest.newBuilder().setName("$name-$it").build()
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

        println("sayHelloWithManyReplies() replies:")
        for (reply in replies)
            println(reply.message)
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
            repeat(5) {
                val request = HelloRequest.newBuilder().setName("$name-$it").build()
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

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}

fun main(args: Array<String>) {
    PrometheusStatsCollector.createAndRegister()
    RpcViews.registerClientGrpcViews()
    val http = HTTPServer("localhost", 8890, true)

    val name = if (args.isNotEmpty()) args[0] else "world"

    HelloWorldClientNoCR("localhost")
            .use { client ->
                client.apply {
                    sayHello(name)
                    sayHelloWithManyRequests(name)
                    sayHelloWithManyReplies(name)
                    sayHelloWithManyRequestsAndReplies(name)
                }
            }

    http.stop()
}
