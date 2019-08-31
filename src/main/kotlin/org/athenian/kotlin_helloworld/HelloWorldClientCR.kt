package org.athenian.kotlin_helloworld

import io.grpc.ManagedChannelBuilder
import io.opencensus.contrib.grpc.metrics.RpcViews
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector
import io.prometheus.client.exporter.HTTPServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.athenain.helloworld.GreeterClient
import org.athenain.helloworld.helloRequest
import java.io.Closeable
import kotlin.random.Random

class HelloWorldClientCR internal constructor(private val client: GreeterClient) : Closeable {

    constructor(host: String, port: Int = 50051) :
            this(GreeterClient.create(channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()))

    fun sayHello(name: String) =
            runBlocking {
                val request = helloRequest { this.name = name }
                val response = client.sayHello(request)
                println("sayHello response: ${response.message}")
            }

    fun sayHelloWithManyRequests(name: String) {
        val call = client.sayHelloWithManyRequests()

        runBlocking {
            launch {
                try {
                    repeat(5) {
                        val request = helloRequest { this.name = "$name-$it" }
                        call.requests.send(request)
                    }
                } finally {
                    call.requests.close()
                }
            }

            val response = call.response.await()
            println("sayHelloWithManyRequests() response: ${response.message}")
        }
    }

    fun sayHelloWithManyReplies(name: String) {
        val request = helloRequest { this.name = name }
        val replies = client.sayHelloWithManyReplies(request).responses

        runBlocking {
            println("sayHelloWithManyReplies() replies:")
            for (reply in replies)
                println(reply.message)
            println()
        }
    }

    fun sayHelloWithManyRequestsAndReplies(name: String) {
        val call = client.sayHelloWithManyRequestsAndReplies()

        runBlocking {
            launch {
                try {
                    repeat(5) {
                        val request = helloRequest { this.name = "$name-$it" }
                        println("sayHelloWithManyRequestsAndReplies() request: ${request.name}")
                        delay(Random.nextLong(1_000))
                        call.requests.send(request)
                    }
                } finally {
                    call.requests.close()
                }
            }

            launch {
                for (reply in call.responses) {
                    delay(Random.nextLong(1_000))
                    println("sayHelloWithManyRequestsAndReplies() response: ${reply.message}")
                }
            }
        }
    }

    override fun close() = client.shutdownChannel()
}

fun main(args: Array<String>) {
    PrometheusStatsCollector.createAndRegister()
    RpcViews.registerClientGrpcViews()
    val http = HTTPServer("localhost", 8889, true)

    val name = if (args.isNotEmpty()) args[0] else "world"

    HelloWorldClientCR("localhost")
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
