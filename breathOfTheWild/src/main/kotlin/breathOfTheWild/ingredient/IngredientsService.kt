package breathOfTheWild.ingredient

import BaseService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.ingredientBonusAddOns.IngredientBonusAddOnsService
import breathOfTheWild.ingredientDuration.IngredientDurationService
import breathOfTheWild.ingredientHearts.IngredientHeartsService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.Ingredient

class IngredientsService(
    private val imagesService: ImagesService,
    private val ingredientHeartsService: IngredientHeartsService,
    private val ingredientBonusAddOnsService: IngredientBonusAddOnsService,
    private val ingredientDurationService: IngredientDurationService
) : BaseService<IngredientsTable, Ingredient>(
    IngredientsTable
) {
    override val IngredientsTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = Ingredient(
        row[table.id],
        row[table.title],
        row[table.subtitle],
        row[table.name],
        imagesService.toItem(row),
        ingredientHeartsService.getFor(row[table.id]),
        row[table.effects],
        row[table.bonus],
        ingredientBonusAddOnsService.getFor(row[table.id]),
        ingredientDurationService.getFor(row[table.id]),
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Ingredient) {
        this[table.title] = item.title
        this[table.subtitle] = item.subtitle
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.effects] = item.effects
        this[table.bonus] = item.bonus
    }
}
