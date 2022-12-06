package client

import java.net.*

fun main(args: Array<String>) {
    try {
        val socket = Socket(args[0], args[1].toInt())
        FileSender(socket, args[2]).apply {
            writeRequest()
            if (readResponse()) {
                println("Error")
                return
            } else {
                wrightFile()
                printResult()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
        return
    }
    return
}