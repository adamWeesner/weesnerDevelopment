package breathOfTheWild.image

import BaseService
import com.weesnerdevelopment.shared.zelda.Image
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class ImagesService : BaseService<ImagesTable, Image>(
    ImagesTable
) {
    override val ImagesTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = Image(
        row[table.id],
        row[table.description],
        row[table.src],
        row[table.width],
        row[table.height],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Image) {
        this[table.description] = item.description
        this[table.src] = item.src
        this[table.width] = item.width
        this[table.height] = item.height
    }
}
