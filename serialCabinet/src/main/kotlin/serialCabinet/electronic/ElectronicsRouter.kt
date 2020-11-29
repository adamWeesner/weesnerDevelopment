package serialCabinet.electronic

import BaseRouter
import shared.serialCabinet.Electronic
import shared.serialCabinet.responses.ElectronicsResponse
import kotlin.reflect.full.createType

data class ElectronicsRouter(
    override val basePath: String,
    override val service: ElectronicsService
) : BaseRouter<Electronic, ElectronicsService>(
    ElectronicsResponse(),
    service,
    Electronic::class.createType()
)
