package breathOfTheWild.ingredient

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.shared.zelda.Ingredient
import com.weesnerdevelopment.shared.zelda.responses.IngredientsResponse
import kotlin.reflect.full.createType

data class IngredientsRouter(
    override val basePath: String,
    override val service: IngredientsService
) : BaseRouter<Ingredient, IngredientsService>(
    IngredientsResponse(),
    service,
    Ingredient::class.createType()
)
