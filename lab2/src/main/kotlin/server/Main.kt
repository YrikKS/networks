import kotlinx.coroutines.*
import server.FileExchange
import java.net.ServerSocket
import java.sql.Time
import java.time.*
import java.util.*
import kotlin.time.Duration.Companion.seconds

suspend fun main(args: Array<String>) = coroutineScope {
    val sock = withContext(Dispatchers.IO) {
        ServerSocket(args[0].toInt())
    }
    while (true) {
        val res = withContext(Dispatchers.IO) {
            sock.accept()
        }
        launch {
            val ex = FileExchange(res)
            val jobPintSpeed = launch {
                ex.printSpeedSend()
            }
            if (!ex.send()) {
                jobPintSpeed.cancel()
                ex.sendError()
            } else {
                jobPintSpeed.cancel()
                ex.sendComplete()
            }
        }
    }
}

