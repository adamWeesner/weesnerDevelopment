package breathOfTheWild.ingredientBonusAddOns

import BaseService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.image.ImagesTable
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class IngredientBonusAddOnsService(
    private val imagesService: ImagesService
) : BaseService<IngredientBonusAddOnsTable, IngredientBonusAddOn>(
    IngredientBonusAddOnsTable
) {
    override val IngredientBonusAddOnsTable.connections: Join?
        get() = null

    suspend fun getFor(id: Int) = getAll {
        IngredientBonusAddOnsTable.itemId eq id
    }?.map {
        val imageId = toItem(it).imageId
        imagesService.get {
            ImagesTable.id eq imageId
        } ?: throw InvalidAttributeException("FrozenFoodEffect Image")
    }

    override suspend fun toItem(row: ResultRow) = IngredientBonusAddOn(
        row[table.id],
        row[table.imageId],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: IngredientBonusAddOn) {
        this[table.imageId] = item.imageId
        this[table.itemId] = item.itemId
    }
}
