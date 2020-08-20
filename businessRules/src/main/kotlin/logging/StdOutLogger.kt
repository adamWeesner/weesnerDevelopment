package logging

import HttpLog
import kimchi.logger.LogLevel
import kimchi.logger.LogWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object StdOutLogger : LogWriter {
    override fun log(level: LogLevel, message: String, cause: Throwable?) {
        if (message.startsWith(HttpLog::class.simpleName!!)) return

        val causedBy = cause ?: ""
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.SSS"))

        println("$now [$level] $message $causedBy")
    }

    override fun shouldLog(level: LogLevel, cause: Throwable?) = true
}