package breathOfTheWild.ingredientHearts

import BaseService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.image.ImagesTable
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class IngredientHeartsService(
    private val imagesService: ImagesService
) : BaseService<IngredientHeartsTable, IngredientHeart>(
    IngredientHeartsTable
) {
    override val IngredientHeartsTable.connections: Join?
        get() = null

    suspend fun getFor(id: Int) = getAll {
        IngredientHeartsTable.itemId eq id
    }?.map {
        val imageId = toItem(it).imageId
        imagesService.get {
            ImagesTable.id eq imageId
        } ?: throw InvalidAttributeException("IngredientHearts Image")
    } ?: throw InvalidAttributeException("IngredientHearts")

    override suspend fun toItem(row: ResultRow) = IngredientHeart(
        row[table.id],
        row[table.imageId],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: IngredientHeart) {
        this[table.imageId] = item.imageId
        this[table.itemId] = item.itemId
    }
}
