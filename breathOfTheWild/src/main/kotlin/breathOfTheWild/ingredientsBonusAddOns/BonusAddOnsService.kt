package breathOfTheWild.ingredientsBonusAddOns

import BaseService
import breathOfTheWild.images.ImagesService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class BonusAddOnsService(
    private val imagesService: ImagesService
) : BaseService<BonusAddOnsTable, BonusAddOn>(
    BonusAddOnsTable
) {
    override val BonusAddOnsTable.connections: Join?
        get() = this.innerJoin(imagesService.table, {
            imageId
        }, {
            id
        })

    suspend fun getForIngredient(id: Int) = getAll {
        BonusAddOnsTable.ingredientId eq id
    }?.mapNotNull {
        imagesService.get { imagesService.table.id eq it[BonusAddOnsTable.imageId] }
    } ?: throw InvalidAttributeException("Bonus Addon for Ingredient")


    override suspend fun toItem(row: ResultRow) = BonusAddOn(
        id = row[table.id],
        imageId = row[BonusAddOnsTable.imageId],
        ingredientId = row[BonusAddOnsTable.ingredientId],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: BonusAddOn) {
        this[BonusAddOnsTable.imageId] = item.imageId
        this[BonusAddOnsTable.ingredientId] = item.ingredientId
    }
}
