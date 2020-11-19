package breathOfTheWild.otherFoodEffect

import BaseService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.image.ImagesTable
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class OtherFoodEffectService(
    private val imagesService: ImagesService
) : BaseService<OtherFoodEffectTable, OtherFoodEffect>(
    OtherFoodEffectTable
) {
    override val OtherFoodEffectTable.connections: Join?
        get() = null

    suspend fun getFor(id: Int) = getAll {
        OtherFoodEffectTable.itemId eq id
    }?.map {
        val imageId = toItem(it).imageId
        imagesService.get {
            ImagesTable.id eq imageId
        } ?: throw InvalidAttributeException("OtherFoodEffect Image")
    } ?: throw InvalidAttributeException("OtherFoodEffect")

    override suspend fun toItem(row: ResultRow) = OtherFoodEffect(
        row[table.id],
        row[table.imageId],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: OtherFoodEffect) {
        this[table.imageId] = item.imageId
        this[table.itemId] = item.itemId
    }
}
