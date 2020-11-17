package breathOfTheWild.roastedFood

import BaseRouter
import shared.zelda.RoastedFood
import shared.zelda.responses.RoastedFoodsResponse
import kotlin.reflect.full.createType

data class RoastedFoodsRouter(
    override val basePath: String,
    override val service: RoastedFoodsService
) : BaseRouter<RoastedFood, RoastedFoodsService>(
    RoastedFoodsResponse(),
    service,
    RoastedFood::class.createType()
)
