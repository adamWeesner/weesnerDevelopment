package breathOfTheWild.elixirs

import BaseService
import breathOfTheWild.elixirIngredients.ElixirIngredientsService
import breathOfTheWild.images.ImagesService
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
            imageId
        }, {
            id
        })

    override suspend fun toItem(row: ResultRow) = Elixir(
        row[table.id],
        row[ElixirsTable.name],
        imagesService.toItem(row),
        row[ElixirsTable.effect],
        row[ElixirsTable.description],
        elixirIngredientsService.getForElixir(row[ElixirsTable.id]),
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Elixir) {
        this[ElixirsTable.name] = item.name
        this[ElixirsTable.imageId] = item.image.id!!
        this[ElixirsTable.effect] = item.effect
        this[ElixirsTable.description] = item.description
    }
}
