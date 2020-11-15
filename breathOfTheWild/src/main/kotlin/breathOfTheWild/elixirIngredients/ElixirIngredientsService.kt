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

    suspend fun getForElixir(id: Int) = getAll {
        ElixirIngredientsTable.elixirId eq id
    }?.mapNotNull {
        toItem(it).name
    } ?: throw InvalidAttributeException("Bonus Addon for Ingredient")


    override suspend fun toItem(row: ResultRow) = ElixirIngredient(
        id = row[table.id],
        name = row[ElixirIngredientsTable.name],
        elixirId = row[ElixirIngredientsTable.elixirId],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: ElixirIngredient) {
        this[ElixirIngredientsTable.name] = item.name
        this[ElixirIngredientsTable.elixirId] = item.elixirId
    }
}
