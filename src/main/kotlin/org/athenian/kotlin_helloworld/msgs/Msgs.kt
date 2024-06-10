package org.athenian.kotlin_helloworld.msgs

import org.athenian.helloworld.HelloReply
import org.athenian.helloworld.HelloRequest

object Msgs {
    fun helloRequest(block: HelloRequest.Builder.() -> Unit): HelloRequest =
        HelloRequest.newBuilder().let { builder ->
            block.invoke(builder)
            builder.build()
        }

    fun helloReply(block: HelloReply.Builder.() -> Unit): HelloReply =
        HelloReply.newBuilder().let { builder ->
            block.invoke(builder)
            builder.build()
        }

}
