package logging

import com.weesnerdevelopment.shared.base.GenericResponse

data class LoggingResponse(
    override var items: List<Logger> = emptyList()
) : GenericResponse<Logger>
