package breathOfTheWild.roastedFood

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.shared.zelda.RoastedFood
import com.weesnerdevelopment.shared.zelda.responses.RoastedFoodsResponse
import kotlin.reflect.full.createType

data class RoastedFoodsRouter(
    override val basePath: String,
    override val service: RoastedFoodsService
) : BaseRouter<RoastedFood, RoastedFoodsService>(
    RoastedFoodsResponse(),
    service,
    RoastedFood::class.createType()
) {
    override fun GenericResponse<RoastedFood>.parse(): String = this.toJson()
}
