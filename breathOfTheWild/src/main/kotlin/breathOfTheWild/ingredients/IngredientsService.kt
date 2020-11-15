package breathOfTheWild.ingredients

import BaseService
import breathOfTheWild.images.ImagesService
import breathOfTheWild.ingredientsBonusAddOns.BonusAddOnsService
import breathOfTheWild.ingredientsDuration.IngredientDurationsService
import breathOfTheWild.ingredientsHearts.IngredientHeartsService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.Ingredient

class IngredientsService(
    private val imagesService: ImagesService,
    private val ingredientHeartsService: IngredientHeartsService,
    private val bonusAddOnsService: BonusAddOnsService,
    private val ingredientDurationsService: IngredientDurationsService
) : BaseService<IngredientsTable, Ingredient>(
    IngredientsTable
) {
    override val IngredientsTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = Ingredient(
        row[table.id],
        row[IngredientsTable.title],
        row[IngredientsTable.subtitle],
        row[IngredientsTable.name],
        imagesService.toItem(row),
        ingredientHeartsService.getForIngredient(row[table.id]),
        row[IngredientsTable.effects],
        row[IngredientsTable.bonus],
        bonusAddOnsService.getForIngredient(row[table.id]),
        ingredientDurationsService.getForIngredient(row[table.id]),
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Ingredient) {
        this[IngredientsTable.title] = item.title
        this[IngredientsTable.subtitle] = item.subtitle
        this[IngredientsTable.name] = item.name
        this[IngredientsTable.effects] = item.effects
        this[IngredientsTable.bonus] = item.bonus
    }
}
