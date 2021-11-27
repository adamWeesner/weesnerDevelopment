package serialCabinet.electronic

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.serialCabinet.Electronic
import com.weesnerdevelopment.shared.serialCabinet.responses.ElectronicsResponse
import com.weesnerdevelopment.shared.toJson
import kotlin.reflect.full.createType

data class ElectronicsRouter(
    override val basePath: String,
    override val service: ElectronicsService
) : BaseRouter<Electronic, ElectronicsService>(
    ElectronicsResponse(),
    service,
    Electronic::class.createType()
)
