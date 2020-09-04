package org.inurl.cfs.core.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * FileServer
 * @author raylax
 */
class FileServer(
        private val host: String = "0.0.0.0",
        private val port: Int = 80) {

    fun start() {
        val bootstrap = ServerBootstrap()
        bootstrap.group(bossGroup, workGroup)
                .handler(LoggingHandler(LogLevel.INFO))
                .channel(NioServerSocketChannel::class.java)
                .childHandler(HttpServerInitializer())
        val future = bootstrap.bind(host, port).sync()
        logger.info("file server started")
        future.channel().closeFuture().sync()
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(FileServer::class.java)
        val bossGroup = NioEventLoopGroup()
        val workGroup = NioEventLoopGroup()
    }

}