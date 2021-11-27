package breathOfTheWild.cookingPotFood

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.shared.zelda.CookingPotFood
import com.weesnerdevelopment.shared.zelda.responses.CookingPotFoodsResponse
import kotlin.reflect.full.createType

data class CookingPotFoodsRouter(
    override val basePath: String,
    override val service: CookingPotFoodsService
) : BaseRouter<CookingPotFood, CookingPotFoodsService>(
    CookingPotFoodsResponse(),
    service,
    CookingPotFood::class.createType()
)
