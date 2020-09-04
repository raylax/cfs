package org.inurl.cfs.core.server

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import org.inurl.cfs.core.server.handler.HttpRequestHandler

/**
 * HttpServerInitializer
 * @author raylax
 */
class HttpServerInitializer : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(HttpRequestDecoder())
        pipeline.addLast(HttpResponseEncoder())
        pipeline.addLast(HttpRequestHandler())
    }

}