package logging

import shared.base.GenericResponse

data class LoggingResponse(
    override var items: List<Logger>? = null
) : GenericResponse<Logger>
