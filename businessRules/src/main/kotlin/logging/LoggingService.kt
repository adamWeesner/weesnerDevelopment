package logging

import BaseService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class LoggingService : BaseService<LoggingTable, Logger>(
    LoggingTable
) {
    override val LoggingTable.connections
        get() = null

    override suspend fun toItem(row: ResultRow) = Logger(
        id = row[table.id],
        log = row[table.log],
        cause = row[table.cause],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Logger) {
        this[table.log] = item.log
    }
}
