package server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import java.net.Socket
import java.time.Duration
import java.util.*

data class FileExchange(private val socket: Socket) {
    private val writer = PrintWriter(socket.outputStream, true)
    private val reader = Scanner(socket.inputStream)
    private var momentSpeed = 0
    private var downloadSize = 0
    private val timeStart = Duration.ofNanos(System.nanoTime())
    private var isEnd = false
    private var nameFile = ""
    private var sizeFile = 0
    private lateinit var file: File

    suspend fun printSpeedSend() {
        while (!isEnd) {
            delay(3000L)
            println(
                "moment speed = $momentSpeed --- total speed = ${
                    downloadSize / (Duration.ofNanos(System.nanoTime()) - timeStart).seconds
                } byte/sec"
            )
        }
    }

    fun sendError() {
        writer.apply {
            try {
                write("Error" + "\n")
                flush()
            } catch (ex: Exception) {
                println("Can't send to client")
            }
        }
    }

    fun sendComplete() {
        writer.apply {
            try {
                write("Complete" + "\n")
                flush()
            } catch (ex: Exception) {
                println("Can't send to client")
            }
        }
    }

    suspend fun send(): Boolean {
        println("start")
        if (!readData()) {
            return false
        }
        if (!createFile()) {
            return false
        }
        try {
            writer.write("start\n")
            writer.flush()
            var outPutFile = file.outputStream()
            var arr = ByteArray(100)
            while (downloadSize < sizeFile) {
                momentSpeed = withContext(Dispatchers.IO) {
                    socket.getInputStream().read(arr)
                }
                withContext(Dispatchers.IO) {
                    outPutFile.write(arr.copyOfRange(0, momentSpeed))
                    outPutFile.flush()
                }
                downloadSize += momentSpeed
            }
        } catch (ex: Exception) {
            println("error with socket")
            return false
        }
        return true
    }

    private fun createFile(): Boolean {
        file = File("uploads/$nameFile")
        return if (file.createNewFile()) {
            true
        } else {
            println("newFile1.txt already exists.")
            false
        }
    }

    private suspend fun readData(): Boolean {
        var readSettings: String = ""
        println("ayyy")
        var byte = ByteArray(100)
        while (readSettings.findAnyOf(listOf("end\n"), 0, false) == null) {
            readSettings += withContext(Dispatchers.IO) {
                val rs = socket.getInputStream().read(byte)
                byte.copyOfRange(0, rs).decodeToString()
            }
        }
        println(readSettings)
        val reg = Regex("(FileName: )([\\w.]+)(\\n)(Size: )([\\d]+)")
        val res = reg.find(readSettings)
        return if (res != null) {
            res.apply {
                nameFile = groups[2]?.value?.toString()!!
                sizeFile = groups[5]?.value?.toInt()!!
            }
            true
        } else {
            false
        }
    }
}