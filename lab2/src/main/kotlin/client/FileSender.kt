package client

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path

data class FileSender(val socket: Socket, val pathFile: String) {
    private val readerFile = File(pathFile).bufferedReader()
    private val sizeFile = Files.size(Path.of(pathFile))
    private val writer = PrintWriter(socket.outputStream, true)
    private val reader = BufferedReader(InputStreamReader(socket.inputStream))


    fun writeRequest() {
        writer.write("FileName: ${pathFile.substringAfterLast("/")}\nSize: $sizeFile\nend\n")
        writer.flush()
    }

    fun readResponse(): Boolean {
        return reader.readLine() != "start"
    }

    fun wrightFile() {
        var sendingByte = 0
        val byteArr = CharArray(100)
        while (sendingByte < sizeFile) {
            val readInMoment = readerFile.read(byteArr)
            if (readInMoment == -1) {
                break
            }
            writer.write(byteArr.copyOfRange(0, readInMoment))
            writer.flush()
            sendingByte += readInMoment
        }
    }

    fun printResult() {
        println(reader.readLine())
    }
}