package org.inurl.cfs.core.server.http

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import java.nio.charset.StandardCharsets

/**
 *
 *
 * @author raylax
 */
class HttpResponse(status: HttpResponseStatus, content: ByteBuf)
    : DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content) {

    constructor(status: HttpResponseStatus) : this(status, Unpooled.EMPTY_BUFFER)

    companion object {
        val OK = HttpResponse(HttpResponseStatus.OK)
        val CONTINUE = HttpResponse(HttpResponseStatus.CONTINUE)
        fun ok(data: String) = r(HttpResponseStatus.OK, Unpooled.copiedBuffer(data, StandardCharsets.UTF_8))
        private fun r(status: HttpResponseStatus, content: ByteBuf): HttpResponse {
            val resp = HttpResponse(status, content)
            resp.headers()[HttpHeaderNames.CONTENT_LENGTH] = content.readableBytes()
            return resp
        }
    }

}