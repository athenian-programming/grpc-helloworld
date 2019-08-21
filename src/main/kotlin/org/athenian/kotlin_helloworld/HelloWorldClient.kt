package org.athenian.kotlin_helloworld

import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.opencensus.contrib.grpc.metrics.RpcViews
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector
import io.opencensus.trace.Tracing
import io.prometheus.client.exporter.HTTPServer
import org.athenain.helloworld.GreeterGrpc
import org.athenain.helloworld.HelloReply
import org.athenain.helloworld.HelloRequest
import java.io.Closeable
import java.lang.Thread.sleep
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

    init {
        channel.notifyWhenStateChanged(ConnectivityState.CONNECTING) { println("Connecting: ${channel.getState(false)}") }
        channel.notifyWhenStateChanged(ConnectivityState.READY) { println("Ready: ${channel.getState(false)}") }
        channel.notifyWhenStateChanged(ConnectivityState.IDLE) { println("Idle: ${channel.getState(false)}") }
    }

    @Throws(InterruptedException::class)
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    override fun close() {
        shutdown()
    }

    fun sayHello(name: String) {
        tracer.spanBuilder("grpc-helloworld.sayHello").startScopedSpan()
                .use {
                    try {
                        val request = HelloRequest.newBuilder().setName(name).build()
                        val response = this.blockingStub.sayHello(request)
                        println("sayHello() response: ${response.message}")
                    } catch (e: StatusRuntimeException) {
                        println("sayHello() failed: ${e.status}")
                    }
                }
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
                val request = HelloRequest.newBuilder()
                        .setName("$name-$it")
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
        replies.forEach { reply -> println(reply.message) }
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

    companion object {
        private val tracer = Tracing.getTracer()

        @JvmStatic
        fun main(args: Array<String>) {

            PrometheusStatsCollector.createAndRegister()
            RpcViews.registerClientGrpcViews()
            val http = HTTPServer("localhost", 8889, true)

            /* Access a service running on the local machine on port 50051 */
            /* Use the arg as the name to greet if provided */
            val name = if (args.isNotEmpty()) args[0] else "world"

            HelloWorldClient("localhost")
                    .use { client ->
                        repeat(10) {
                            client.sayHello(name)
                            client.sayHelloWithManyRequests(name)
                            client.sayHelloWithManyReplies(name)
                            client.sayHelloWithManyRequestsAndReplies(name)

                            sleep(1000)
                        }
                    }

            http.stop()
        }
    }
}
