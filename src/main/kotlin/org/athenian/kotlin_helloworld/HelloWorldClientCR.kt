package org.athenian.kotlin_helloworld

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.opencensus.contrib.grpc.metrics.RpcViews
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector
import io.prometheus.client.exporter.HTTPServer
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.athenian.helloworld.GreeterGrpcKt.GreeterCoroutineStub
import org.athenian.helloworld.HelloRequest
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun helloRequest(block: HelloRequest.Builder.() -> Unit): HelloRequest =
    HelloRequest.newBuilder().let {
        block.invoke(it)
        it.build()
    }

class HelloWorldClientCR internal constructor(private val channel: ManagedChannel) : Closeable {
    private val stub = GreeterCoroutineStub(channel)

    constructor(host: String, port: Int = 50051) :
            this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build())

    fun sayHello(name: String) =
        runBlocking {
            val request = helloRequest { this.name = name }
            val response = stub.sayHello(request)
            println("sayHello response: ${response.message}")
        }

    fun sayHelloWithManyRequests(name: String) {
        val requests =
            flow {
                repeat(5) {
                    val request = helloRequest { this.name = "$name-$it" }
                    emit(request)
                }
            }

        runBlocking {
            val response = stub.sayHelloWithManyRequests(requests)
            println("sayHelloWithManyRequests() response: ${response.message}")
        }
    }

    fun sayHelloWithManyReplies(name: String) {
        val request = helloRequest { this.name = name }
        val replies = stub.sayHelloWithManyReplies(request)

        runBlocking {
            println("sayHelloWithManyReplies() replies:")
            replies.collect { reply ->
                println(reply.message)
            }
            println()
        }
    }

    fun sayHelloWithManyRequestsAndReplies(name: String) {
        runBlocking {
            val requests =
                flow {
                    repeat(5) {
                        delay(Random.nextLong(1_000))
                        val request = helloRequest { this.name = "$name-$it" }
                        println("sayHelloWithManyRequestsAndReplies() request: ${request.name}")
                        emit(request)
                    }
                }
            val replies = stub.sayHelloWithManyRequestsAndReplies(requests)
            replies.collect {
                delay(Random.nextLong(1_000))
                println("sayHelloWithManyRequestsAndReplies() response: ${it.message}")
            }
        }
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PrometheusStatsCollector.createAndRegister()
            RpcViews.registerClientGrpcViews()
            val http = HTTPServer("localhost", 8889, true)

            val name = if (args.isNotEmpty()) args[0] else "world"

            Executors.newFixedThreadPool(10).asCoroutineDispatcher().use { dispatcher ->
                val builder = ManagedChannelBuilder.forTarget("localhost:50051").usePlaintext()
                val channel = builder.executor(dispatcher.asExecutor()).build()
                val c = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
                HelloWorldClientCR(c)
                    .use { client ->
                        client.apply {
                            sayHello(name)
                            sayHelloWithManyRequests(name)
                            sayHelloWithManyReplies(name)
                            sayHelloWithManyRequestsAndReplies(name)
                        }
                    }
            }
            http.stop()
        }
    }
}

