package org.athenian.kotlin_helloworld.withCR

import org.athenian.helloworld.HelloReply
import org.athenian.helloworld.HelloRequest

object Msgs {
    fun helloRequest(block: HelloRequest.Builder.() -> Unit): HelloRequest =
        HelloRequest.newBuilder().let {
            block.invoke(it)
            it.build()
        }

    fun helloReply(block: HelloReply.Builder.() -> Unit): HelloReply =
        HelloReply.newBuilder().let {
            block.invoke(it)
            it.build()
        }

}