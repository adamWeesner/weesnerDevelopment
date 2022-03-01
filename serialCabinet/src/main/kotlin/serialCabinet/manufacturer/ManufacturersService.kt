package serialCabinet.manufacturer

import BaseService
import com.weesnerdevelopment.shared.serialCabinet.Manufacturer
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class ManufacturersService : BaseService<ManufacturersTable, Manufacturer>(
    ManufacturersTable
) {
    override val ManufacturersTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = Manufacturer(
        row[table.id],
        row[table.name],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Manufacturer) {
        this[table.name] = item.name
    }
}
