package logging

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlin.reflect.full.createType

class LoggingRouter(
    override val basePath: String,
    service: LoggingService
) : BaseRouter<Logger, LoggingService>(
    LoggingResponse(),
    service,
    Logger::class.createType()
) {
    override fun GenericResponse<Logger>.parse(): String = this.toJson()

    override fun Route.setupRoutes() {
        route("/$basePath") {
            addRequest()
            getRequest()
            updateRequest()
            deleteRequest()
            wsGetRequest()
        }
    }

    fun Route.wsGetRequest() {
        webSocket {
            for (frame in incoming) {
                outgoing.send(Frame.Text(String(frame.readBytes())))
            }
        }
    }
}