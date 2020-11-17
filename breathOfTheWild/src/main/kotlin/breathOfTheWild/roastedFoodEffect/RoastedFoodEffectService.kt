package breathOfTheWild.roastedFoodEffect

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class RoastedFoodEffectService : BaseService<RoastedFoodEffectTable, RoastedFoodEffect>(
    RoastedFoodEffectTable
) {
    override val RoastedFoodEffectTable.connections: Join?
        get() = null
        
    suspend fun getFor(id: Int) = getAll {
        RoastedFoodEffectTable.itemId eq id
    }?.map { toItem(it) }?: throw InvalidAttributeException("RoastedFoodEffect")

    override suspend fun toItem(row: ResultRow) = RoastedFoodEffect(
        row[table.id],
        row[table.imageId],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: RoastedFoodEffect) {
        this[table.imageId] = item.imageId
        this[table.itemId] = item.itemId
    }
}
