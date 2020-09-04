package org.inurl.cfs.core.server.handler

import io.netty.channel.*
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpMethod.GET
import io.netty.handler.codec.http.HttpMethod.POST
import io.netty.util.ReferenceCountUtil
import org.inurl.cfs.core.server.EXPECT_100_CONTINUE
import org.inurl.cfs.core.server.file.FileSaver
import org.inurl.cfs.core.server.http.HttpResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.RandomAccessFile


/**
 * HttpRequestHandler
 * @author raylax
 */
class HttpRequestHandler : ChannelInboundHandlerAdapter() {

    private val fileSaver = FileSaver()
    private lateinit var method: HttpMethod

    private fun handleHttpRequest(ctx: ChannelHandlerContext, httpRequest: HttpRequest) {
        method = httpRequest.method()
        logger.info("{} {}", method, httpRequest.uri())
        when (method) {
            POST -> handlePost(ctx, httpRequest)
            GET -> handleGet(ctx, httpRequest)
            else -> {
                ctx.writeAndFlush(HttpResponse.UNSUPPORTED_METHOD)
                ctx.close()
            }
        }
    }

    private fun handleGet(ctx: ChannelHandlerContext, httpRequest: HttpRequest) {
        val file = File("/Users/apple/Downloads${httpRequest.uri()}")
        if (!file.exists() || file.isDirectory) {
            ctx.writeAndFlush(HttpResponse.NOT_FOUND)
            ctx.close()
            return
        }
        val response = HttpResponse.file(file)
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        ctx.write(response)
        ctx.write(DefaultFileRegion(RandomAccessFile(file, "r").channel, 0,
                file.length()), ctx.newProgressivePromise())
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener { ChannelFutureListener.CLOSE }
    }

    private fun handlePost(ctx: ChannelHandlerContext, httpRequest: HttpRequest) {
        val headers = httpRequest.headers()
        if (headers[HttpHeaderNames.EXPECT] == EXPECT_100_CONTINUE) {
            ctx.writeAndFlush(HttpResponse.CONTINUE)
        }
        fileSaver.open(httpRequest.uri())
        ctx.channel().closeFuture().addListener {
            fileSaver.release()
        }
    }

    private fun handleHttpContent(ctx: ChannelHandlerContext, httpContent: HttpContent) {
        if (method == POST) fileSaver.append(httpContent.content())
    }

    private fun handleLastHttpContent(ctx: ChannelHandlerContext, lastHttpContent: LastHttpContent) {
        if (method == POST) {
            fileSaver.append(lastHttpContent.content())
            fileSaver.close()
            ctx.writeAndFlush(HttpResponse.ok("""{"status", 0}"""))
            ctx.close()
        }
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

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.warn("exceptionCaught [${ctx.channel()}]", cause)
        if (ctx.channel().isActive) {
            ctx.write(HttpResponse)
            ctx.close()
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(HttpRequestHandler::class.java);
    }

}