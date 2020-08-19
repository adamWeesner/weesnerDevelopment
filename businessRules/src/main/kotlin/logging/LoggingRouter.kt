package logging

import BaseRouter
import kotlin.reflect.full.createType

class LoggingRouter(
    override val basePath: String,
    service: LoggingService
) : BaseRouter<Logger, LoggingService>(
    LoggingResponse(),
    service,
    Logger::class.createType()
)