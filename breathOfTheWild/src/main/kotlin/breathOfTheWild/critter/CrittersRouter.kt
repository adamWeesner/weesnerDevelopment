package breathOfTheWild.critter

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.shared.zelda.Critter
import com.weesnerdevelopment.shared.zelda.responses.CrittersResponse
import kotlin.reflect.full.createType

data class CrittersRouter(
    override val basePath: String,
    override val service: CrittersService
) : BaseRouter<Critter, CrittersService>(
    CrittersResponse(),
    service,
    Critter::class.createType()
) {
    override fun GenericResponse<Critter>.parse(): String = this.toJson()
}
