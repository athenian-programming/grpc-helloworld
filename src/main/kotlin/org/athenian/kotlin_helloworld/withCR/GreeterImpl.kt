package org.athenian.kotlin_helloworld.withCR

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.athenian.helloworld.GreeterGrpcKt
import org.athenian.helloworld.HelloReply
import org.athenian.helloworld.HelloRequest

// https://github.com/GoogleCloudPlatform/kotlin-samples/blob/master/run/grpc-hello-world-streaming/src/main/kotlin/io/grpc/examples/helloworld/HelloWorldServer.kt

class GreeterImpl : GreeterGrpcKt.GreeterCoroutineImplBase() {

    override suspend fun sayHello(request: HelloRequest): HelloReply =
        helloReply { message = "Hello ${request.name}" }

    override suspend fun sayHelloWithManyRequests(requests: Flow<HelloRequest>): HelloReply {
        val names: MutableList<String> = mutableListOf()
        requests.collect { names.add(it.name) }
        return helloReply { message = "Hello ${names.joinToString(", ")}" }
    }

    override fun sayHelloWithManyReplies(request: HelloRequest): Flow<HelloReply> =
        flow {
            repeat(5) { i ->
                val reply = helloReply { message = "Hello ${request.name} [$i]" }
                emit(reply)
            }
        }

    override fun sayHelloWithManyRequestsAndReplies(requests: Flow<HelloRequest>): Flow<HelloReply> =
        flow {
            val requestFlow =
                flow {
                    requests.collect {
                        emit(it)
                    }
                }
            requestFlow.collect { request ->
                repeat(5) {
                    val reply = helloReply { message = "Hello ${request.name} [$it]" }
                    emit(reply)
                }
            }
        }

    companion object {
        fun helloReply(block: HelloReply.Builder.() -> Unit): HelloReply =
            HelloReply.newBuilder().let {
                block.invoke(it)
                it.build()
            }
    }
}