package breathOfTheWild.ingredient

import BaseRouter
import shared.zelda.Ingredient
import shared.zelda.responses.IngredientsResponse
import kotlin.reflect.full.createType

data class IngredientsRouter(
    override val basePath: String,
    override val service: IngredientsService
) : BaseRouter<Ingredient, IngredientsService>(
    IngredientsResponse(),
    service,
    Ingredient::class.createType()
)
