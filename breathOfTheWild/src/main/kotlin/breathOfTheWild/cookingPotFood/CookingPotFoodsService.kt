package breathOfTheWild.cookingPotFood

import BaseService
import breathOfTheWild.cookingPotFoodIngredients.CookingPotFoodIngredientsService
import breathOfTheWild.image.ImagesService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.CookingPotFood

class CookingPotFoodsService(
    private val imagesService: ImagesService,
    private val cookingPotFoodIngredientsService: CookingPotFoodIngredientsService
) : BaseService<CookingPotFoodsTable, CookingPotFood>(
    CookingPotFoodsTable
) {
    override val CookingPotFoodsTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = CookingPotFood(
        row[table.id],
        row[table.name],
        imagesService.toItem(row),
        row[table.description],
        cookingPotFoodIngredientsService.getFor(row[table.id]),
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: CookingPotFood) {
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.description] = item.description
    }
}
