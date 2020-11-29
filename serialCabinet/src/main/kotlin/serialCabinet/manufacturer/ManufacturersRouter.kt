package serialCabinet.manufacturer

import BaseRouter
import shared.serialCabinet.Manufacturer
import shared.serialCabinet.responses.ManufacturersResponse
import kotlin.reflect.full.createType

data class ManufacturersRouter(
    override val basePath: String,
    override val service: ManufacturersService
) : BaseRouter<Manufacturer, ManufacturersService>(
    ManufacturersResponse(),
    service,
    Manufacturer::class.createType()
)
