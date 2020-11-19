package breathOfTheWild.elixir

import BaseService
import breathOfTheWild.elixirIngredients.ElixirIngredientsService
import breathOfTheWild.image.ImagesService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.Elixir

class ElixirsService(
    private val imagesService: ImagesService,
    private val elixirIngredientsService: ElixirIngredientsService
) : BaseService<ElixirsTable, Elixir>(
    ElixirsTable
) {
    override val ElixirsTable.connections: Join?
        get() = this.innerJoin(imagesService.table, {
            image
        }, {
            id
        })

    override suspend fun toItem(row: ResultRow) = Elixir(
        row[table.id],
        row[table.name],
        imagesService.toItem(row),
        row[table.effect],
        row[table.description],
        elixirIngredientsService.getFor(row[table.id]),
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Elixir) {
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.effect] = item.effect
        this[table.description] = item.description
    }
}
