package org.inurl.cfs.core.server.file

import io.netty.buffer.ByteBuf
import java.io.File
import java.io.FileOutputStream


/**
 * no thread safe
 */
class FileSaver {

    private lateinit var file: File
    private lateinit var fos: FileOutputStream
    private var closed = true

    fun open(path: String) {
        file = File("/Users/apple/Downloads$path")
        fos = FileOutputStream(file)
        closed = false
    }

    fun append(data: ByteBuf) {
        fos.channel.write(data.nioBuffer())
    }

    fun close() {
        fos.channel.force(false)
        if (!closed) {
            fos.close()
            closed = true
        }
    }

    fun release() {
        if (closed) {
            return
        }
        close()
        file.delete()
    }


}