package org.inurl.cfs.core.server.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.LastHttpContent
import io.netty.util.ReferenceCountUtil
import org.inurl.cfs.core.server.EXPECT_100_CONTINUE
import org.inurl.cfs.core.server.http.HttpResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * HttpRequestHandler
 * @author raylax
 */
class HttpRequestHandler : ChannelInboundHandlerAdapter() {

    private fun handleHttpRequest(ctx: ChannelHandlerContext, httpRequest: HttpRequest) {
        val headers = httpRequest.headers()
        logger.info("headers = {}", headers)
        if (headers[HttpHeaderNames.EXPECT] == EXPECT_100_CONTINUE) {
            ctx.writeAndFlush(HttpResponse.CONTINUE)
        }
    }

    private fun handleHttpContent(ctx: ChannelHandlerContext, httpContent: HttpContent) {

    }

    private fun handleLastHttpContent(ctx: ChannelHandlerContext, lastHttpContent: LastHttpContent) {
        ctx.close()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        var release = true
        try {
            when (msg) {
                is HttpRequest -> handleHttpRequest(ctx, msg)
                is LastHttpContent -> handleLastHttpContent(ctx, msg)
                is HttpContent -> handleHttpContent(ctx, msg)
                else -> {
                    release = false
                    ctx.fireChannelRead(msg)
                }
            }
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg)
            }
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(HttpRequestHandler::class.java);
    }

}