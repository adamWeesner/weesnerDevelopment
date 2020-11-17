package breathOfTheWild.frozenFoodEffect

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class FrozenFoodEffectService : BaseService<FrozenFoodEffectTable, FrozenFoodEffect>(
    FrozenFoodEffectTable
) {
    override val FrozenFoodEffectTable.connections: Join?
        get() = null
        
    suspend fun getFor(id: Int) = getAll {
        FrozenFoodEffectTable.itemId eq id
    }?.map { toItem(it) }?: throw InvalidAttributeException("FrozenFoodEffect")

    override suspend fun toItem(row: ResultRow) = FrozenFoodEffect(
        row[table.id],
        row[table.imageId],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: FrozenFoodEffect) {
        this[table.imageId] = item.imageId
        this[table.itemId] = item.itemId
    }
}
