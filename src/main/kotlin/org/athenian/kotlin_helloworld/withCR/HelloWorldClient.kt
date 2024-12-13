package org.athenian.kotlin_helloworld.withCR

import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.athenian.helloworld.GreeterGrpcKt.GreeterCoroutineStub
import org.athenian.helloworld.HelloRequest
import org.athenian.kotlin_helloworld.msgs.Msgs.helloRequest
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

// https://github.com/GoogleCloudPlatform/kotlin-samples/blob/master/run/grpc-hello-world-streaming/src/main/kotlin/io/grpc/examples/helloworld/HelloWorldClient.kt

class HelloWorldClient internal constructor(private val channel: ManagedChannel) : Closeable {
    constructor(host: String, port: Int = 50051) :
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build())

    private val stub = GreeterCoroutineStub(channel)

    init {
        with(channel) {
            notifyWhenStateChanged(ConnectivityState.CONNECTING) { println("Connecting: ${getState(false)}") }
            notifyWhenStateChanged(ConnectivityState.READY) { println("Ready: ${getState(false)}") }
            notifyWhenStateChanged(ConnectivityState.IDLE) { println("Idle: ${getState(false)}") }
        }
    }

    suspend fun sayHello(name: String) {
        val request = helloRequest { this.name = name }
        val response = stub.sayHello(request)
        println("sayHello response: ${response.message}")
    }

    suspend fun sayHelloWithManyRequests(name: String) {
        val requests =
            flow {
                repeat(5) {
                    val request = helloRequest { this.name = "$name-$it" }
                    emit(request)
                }
            }
        val response = stub.sayHelloWithManyRequests(requests)
        println("sayHelloWithManyRequests() response: ${response.message}")
    }

    suspend fun sayHelloWithManyReplies(name: String) {
        val request = helloRequest { this.name = name }
        val replies = stub.sayHelloWithManyReplies(request)
        println("sayHelloWithManyReplies() replies:")
        replies.collect { reply -> println(reply.message) }
        println()
    }

    suspend fun sayHelloWithManyRequestsAndReplies(name: String) {
        val requests =
            flow {
                repeat(5) {
                    delay(Random.nextLong(1_000).milliseconds)
                    val request =
                        HelloRequest
                            .newBuilder()
                            .let { builder ->
                                builder.name = "$name-$builder"
                                builder.build()
                            }
                    println("sayHelloWithManyRequestsAndReplies() request: $request")
                    emit(request)
                }
            }
        val replies = stub.sayHelloWithManyRequestsAndReplies(requests)
        replies.collect {
            delay(Random.nextLong(1_000).milliseconds)
            println("sayHelloWithManyRequestsAndReplies() response: $it")
        }
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val name = if (args.isNotEmpty()) args[0] else "world"

            runBlocking {
                HelloWorldClient("localhost")
                    .use { client ->
                        with(client) {
                            sayHello(name)
                            sayHelloWithManyRequests(name)
                            sayHelloWithManyReplies(name)
                            sayHelloWithManyRequestsAndReplies(name)
                        }
                    }
            }
        }
    }
}

