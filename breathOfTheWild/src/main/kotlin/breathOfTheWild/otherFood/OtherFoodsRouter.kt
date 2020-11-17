package breathOfTheWild.otherFood

import BaseRouter
import shared.zelda.OtherFood
import shared.zelda.responses.OtherFoodsResponse
import kotlin.reflect.full.createType

data class OtherFoodsRouter(
    override val basePath: String,
    override val service: OtherFoodsService
) : BaseRouter<OtherFood, OtherFoodsService>(
    OtherFoodsResponse(),
    service,
    OtherFood::class.createType()
)
