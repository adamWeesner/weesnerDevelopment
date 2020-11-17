package breathOfTheWild.frozenFood

import BaseRouter
import shared.zelda.FrozenFood
import shared.zelda.responses.FrozenFoodsResponse
import kotlin.reflect.full.createType

data class FrozenFoodsRouter(
    override val basePath: String,
    override val service: FrozenFoodsService
) : BaseRouter<FrozenFood, FrozenFoodsService>(
    FrozenFoodsResponse(),
    service,
    FrozenFood::class.createType()
)
