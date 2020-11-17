package breathOfTheWild.effect

import BaseService
import breathOfTheWild.image.ImagesService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.Effect

class EffectsService(
    private val imagesService: ImagesService
) : BaseService<EffectsTable, Effect>(
    EffectsTable
) {
    override val EffectsTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = Effect(
        row[table.id],
        row[table.name],
        imagesService.toItem(row),
        row[table.description],
        row[table.timeLimit],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Effect) {
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.description] = item.description
        this[table.timeLimit] = item.timeLimit
    }
}
