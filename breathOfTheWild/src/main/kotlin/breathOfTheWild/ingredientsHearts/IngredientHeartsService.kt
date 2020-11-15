package breathOfTheWild.ingredientsHearts

import BaseService
import breathOfTheWild.images.ImagesService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class IngredientHeartsService(
    private val imagesService: ImagesService
) : BaseService<IngredientHeartsTable, IngredientHearts>(
    IngredientHeartsTable
) {
    override val IngredientHeartsTable.connections: Join?
        get() = this.innerJoin(imagesService.table, {
            imageId
        }, {
            id
        })

    suspend fun getForIngredient(id: Int) = getAll {
        IngredientHeartsTable.ingredientId eq id
    }?.mapNotNull {
        imagesService.get { imagesService.table.id eq it[IngredientHeartsTable.imageId] }
    } ?: throw InvalidAttributeException("Bill categories")


    override suspend fun toItem(row: ResultRow) = IngredientHearts(
        id = row[table.id],
        imageId = row[IngredientHeartsTable.imageId],
        ingredientId = row[IngredientHeartsTable.ingredientId],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: IngredientHearts) {
        this[IngredientHeartsTable.imageId] = item.imageId
        this[IngredientHeartsTable.ingredientId] = item.ingredientId
    }
}
