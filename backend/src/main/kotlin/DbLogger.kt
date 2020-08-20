package com.weesnerdevelopment

import kimchi.logger.LogLevel
import kimchi.logger.LogWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import logging.Logger
import logging.LoggingService

object DbLogger : LogWriter {
    private val scope = CoroutineScope(Dispatchers.Default)
    var service: LoggingService? = null

    override fun log(level: LogLevel, message: String, cause: Throwable?) {

        val logger = Logger(
            log = message,
            cause = if (cause != null) cause::class.qualifiedName else null
        )
        scope.launch {
            service?.add(logger)
        }
    }

    override fun shouldLog(level: LogLevel, cause: Throwable?): Boolean {
        return when (level) {
            LogLevel.INFO, LogLevel.WARNING, LogLevel.ERROR -> true
            else -> false
        }
    }
}