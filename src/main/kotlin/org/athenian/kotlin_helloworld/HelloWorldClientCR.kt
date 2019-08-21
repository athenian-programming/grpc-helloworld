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
import kotlin.random.Random
import kotlin.system.exitProcess


fun main() {
    try {
        PrometheusStatsCollector.createAndRegister()
        RpcViews.registerClientGrpcViews()
        val http = HTTPServer("localhost", 8889, true)

        GreeterClient.create(channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build())
                .also { client ->
                    syncClient(client)
                    streamingClient(client)
                    streamingServer(client)
                    bidirectionalService(client)

                    client.shutdownChannel()
                }

        exitProcess(0)
    } catch (t: Throwable) {
        System.err.println("Failed: $t")
    }
    exitProcess(1)
}

fun syncClient(client: GreeterClient) =
        runBlocking {
            val request =
                    helloRequest {
                        name = "Hello!"
                    }

            val response = client.sayHello(request)
            println("Sync response was: ${response.message}")
        }

fun streamingClient(client: GreeterClient) =
        runBlocking {
            client.sayHelloWithManyRequests()
                    .also { call ->

                        launch {
                            repeat(5) {
                                val request = helloRequest { name = "Hello Again! $it" }
                                call.requests.send(request)
                            }
                            call.requests.close()
                        }

                        val response = call.response.await()
                        println("Streaming Client result = ${response.message}")
                    }
        }

fun streamingServer(client: GreeterClient) =
        runBlocking {
            val request = helloRequest { name = "Bill" }
            client.sayHelloWithManyReplies(request)
                    .also { call ->
                        for (response in call.responses)
                            println("Streaming Server result = ${response.message}")
                    }
        }

fun bidirectionalService(client: GreeterClient) =
        runBlocking {
            client.sayHelloWithManyRequestsAndReplies()
                    .also { call ->

                        launch {
                            repeat(5) {
                                val s = "Mary $it"
                                val request = helloRequest { name = s }
                                delay(Random.nextLong(1_000))
                                call.requests.send(request)
                                println("Async client sent $s")
                            }
                            call.requests.close()
                        }

                        launch {
                            for (response in call.responses) {
                                delay(Random.nextLong(1_000))
                                println("Async response from server = ${response.message}")
                            }
                        }
                    }
        }


