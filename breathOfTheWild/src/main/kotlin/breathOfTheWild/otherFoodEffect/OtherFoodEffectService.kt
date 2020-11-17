package breathOfTheWild.otherFoodEffect

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class OtherFoodEffectService : BaseService<OtherFoodEffectTable, OtherFoodEffect>(
    OtherFoodEffectTable
) {
    override val OtherFoodEffectTable.connections: Join?
        get() = null
        
    suspend fun getFor(id: Int) = getAll {
        OtherFoodEffectTable.itemId eq id
    }?.map { toItem(it) }?: throw InvalidAttributeException("OtherFoodEffect")

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
