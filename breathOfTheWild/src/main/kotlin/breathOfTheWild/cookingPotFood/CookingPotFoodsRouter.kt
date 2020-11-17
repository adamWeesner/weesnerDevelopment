package breathOfTheWild.cookingPotFood

import BaseRouter
import shared.zelda.CookingPotFood
import shared.zelda.responses.CookingPotFoodsResponse
import kotlin.reflect.full.createType

data class CookingPotFoodsRouter(
    override val basePath: String,
    override val service: CookingPotFoodsService
) : BaseRouter<CookingPotFood, CookingPotFoodsService>(
    CookingPotFoodsResponse(),
    service,
    CookingPotFood::class.createType()
)
