package breathOfTheWild.frozenFood

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.shared.zelda.FrozenFood
import com.weesnerdevelopment.shared.zelda.responses.FrozenFoodsResponse
import kotlin.reflect.full.createType

data class FrozenFoodsRouter(
    override val basePath: String,
    override val service: FrozenFoodsService
) : BaseRouter<FrozenFood, FrozenFoodsService>(
    FrozenFoodsResponse(),
    service,
    FrozenFood::class.createType()
) {
    override fun GenericResponse<FrozenFood>.parse(): String = this.toJson()
}
