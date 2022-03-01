package serialCabinet.manufacturer

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.serialCabinet.Manufacturer
import com.weesnerdevelopment.shared.serialCabinet.responses.ManufacturersResponse
import com.weesnerdevelopment.shared.toJson
import kotlin.reflect.full.createType

data class ManufacturersRouter(
    override val basePath: String,
    override val service: ManufacturersService
) : BaseRouter<Manufacturer, ManufacturersService>(
    ManufacturersResponse(),
    service,
    Manufacturer::class.createType()
) {
    override fun GenericResponse<Manufacturer>.parse(): String = this.toJson()
}
