package breathOfTheWild.images

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.Image

class ImagesService : BaseService<ImagesTable, Image>(
    ImagesTable
) {
    override val ImagesTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = Image(
        row[table.id],
        row[ImagesTable.description],
        row[ImagesTable.src],
        row[ImagesTable.width],
        row[ImagesTable.height],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Image) {
        this[ImagesTable.description] = item.description
        this[ImagesTable.src] = item.src
        this[ImagesTable.width] = item.width
        this[ImagesTable.height] = item.height
    }
}
