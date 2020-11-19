package breathOfTheWild.elixirIngredients

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class ElixirIngredientsService : BaseService<ElixirIngredientsTable, ElixirIngredient>(
    ElixirIngredientsTable
) {
    override val ElixirIngredientsTable.connections: Join?
        get() = null

    suspend fun getFor(id: Int) = getAll {
        ElixirIngredientsTable.itemId eq id
    }?.map { toItem(it).ingredient } ?: throw InvalidAttributeException("ElixirIngredients")

    override suspend fun toItem(row: ResultRow) = ElixirIngredient(
        row[table.id],
        row[table.ingredient],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: ElixirIngredient) {
        this[table.ingredient] = item.ingredient
        this[table.itemId] = item.itemId
    }
}
