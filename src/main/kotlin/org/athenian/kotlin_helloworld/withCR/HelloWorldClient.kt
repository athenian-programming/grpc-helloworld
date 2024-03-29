package org.athenian.kotlin_helloworld.withCR

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.athenian.helloworld.GreeterGrpcKt.GreeterCoroutineStub
import org.athenian.helloworld.krotodc.HelloRequest
import org.athenian.helloworld.krotodc.helloreply.toDataClass
import org.athenian.helloworld.krotodc.hellorequest.toProto
import org.athenian.kotlin_helloworld.withCR.Msgs.helloRequest
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// https://github.com/GoogleCloudPlatform/kotlin-samples/blob/master/run/grpc-hello-world-streaming/src/main/kotlin/io/grpc/examples/helloworld/HelloWorldClient.kt

class HelloWorldClient internal constructor(private val channel: ManagedChannel) : Closeable {
    constructor(host: String, port: Int = 50051) :
            this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build())

    private val stub = GreeterCoroutineStub(channel)

    suspend fun sayHello(name: String) =
        coroutineScope {
            val request = helloRequest { this.name = name }
            val response = stub.sayHello(request)
            println("sayHello response: ${response.message}")
        }

    suspend fun sayHelloWithManyRequests(name: String) =
        coroutineScope {
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

    suspend fun sayHelloWithManyReplies(name: String) =
        coroutineScope {
            val request = helloRequest { this.name = name }
            val replies = stub.sayHelloWithManyReplies(request)
            println("sayHelloWithManyReplies() replies:")
            replies.collect { reply -> println(reply.message) }
            println()
        }

    suspend fun sayHelloWithManyRequestsAndReplies(name: String) =
        coroutineScope {
            val requests =
                flow {
                    repeat(5) {
                        delay(Random.nextLong(1_000))
                        val request = HelloRequest(name = "$name-$it")
                        println("sayHelloWithManyRequestsAndReplies() request: $request")
                        emit(request.toProto())
                    }
                }
            val replies = stub.sayHelloWithManyRequestsAndReplies(requests)
            replies.collect {
                delay(Random.nextLong(1_000))
                println("sayHelloWithManyRequestsAndReplies() response: ${it.toDataClass()}")
            }
        }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val name = if (args.isNotEmpty()) args[0] else "world"

            HelloWorldClient("localhost")
                .use { client ->
                    runBlocking {
                        client.apply {
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

