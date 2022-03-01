package breathOfTheWild.otherFood

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.shared.zelda.OtherFood
import com.weesnerdevelopment.shared.zelda.responses.OtherFoodsResponse
import kotlin.reflect.full.createType

data class OtherFoodsRouter(
    override val basePath: String,
    override val service: OtherFoodsService
) : BaseRouter<OtherFood, OtherFoodsService>(
    OtherFoodsResponse(),
    service,
    OtherFood::class.createType()
) {
    override fun GenericResponse<OtherFood>.parse(): String = this.toJson()
}
