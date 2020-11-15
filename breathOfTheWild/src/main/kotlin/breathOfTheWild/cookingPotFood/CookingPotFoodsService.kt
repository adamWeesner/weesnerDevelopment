package breathOfTheWild.cookingPotFood

import BaseService
import breathOfTheWild.cookingPotIngredients.CookingPotIngredientsService
import breathOfTheWild.images.ImagesService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.CookingPotFood

class CookingPotFoodsService(
    private val imagesService: ImagesService,
    private val cookingPotIngredientsService: CookingPotIngredientsService
) : BaseService<CookingPotFoodsTable, CookingPotFood>(
    CookingPotFoodsTable
) {
    override val CookingPotFoodsTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = CookingPotFood(
        row[table.id],
        row[CookingPotFoodsTable.name],
        imagesService.toItem(row),
        row[CookingPotFoodsTable.description],
        cookingPotIngredientsService.getForFood(row[table.id]),
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: CookingPotFood) {
        this[CookingPotFoodsTable.name] = item.name
        this[CookingPotFoodsTable.imageId] = item.image.id!!
        this[CookingPotFoodsTable.description] = item.description
    }
}
