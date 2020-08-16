package com.weesnerdevelopment.validator

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class ValidatorService(
    override val table: ValidatorTable = ValidatorTable
) : BaseService<ValidatorTable, ValidatorItem>(
    table
) {
    override val ValidatorTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = ValidatorItem(
        id = row[table.id],
        name = row[table.name],
        amount = row[table.amount],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: ValidatorItem) {
        this[table.name] = item.name
        this[table.amount] = item.amount
    }
}
