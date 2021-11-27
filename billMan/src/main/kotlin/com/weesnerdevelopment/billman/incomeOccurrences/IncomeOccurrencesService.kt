package com.weesnerdevelopment.billman.incomeOccurrences

import BaseService
import auth.UsersService
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import com.weesnerdevelopment.shared.billMan.IncomeOccurrence
import diff
import history.HistoryService
import isNotValidId
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class IncomeOccurrencesService(
    private val usersService: UsersService,
    private val historyService: HistoryService
) : BaseService<IncomeOccurrencesTable, IncomeOccurrence>(
    IncomeOccurrencesTable
) {
    override val IncomeOccurrencesTable.connections
        get() = this.innerJoin(usersService.table, {
            ownerId
        }, {
            uuid
        })

    override suspend fun update(item: IncomeOccurrence, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
        val oldItem = get {
            table.id eq item.id!!
        } ?: return null

        oldItem.diff(item).updates(item.owner).forEach {
            val history = historyService.add(it)

            if (history.isNotValidId)
                return history
        }

        return super.update(item, op)
    }

    override suspend fun toItem(row: ResultRow) = IncomeOccurrence(
        id = row[table.id],
        owner = usersService.toItemRedacted(row),
        amount = row[table.amount],
        itemId = row[table.itemId].toString(),
        dueDate = row[table.dueDate],
        every = row[table.every],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated],
        amountLeft = "0"
    ).let {
        val history = historyService.getFor<IncomeOccurrence>(it.id, it.owner)

        return@let if (history == null)
            it
        else
            it.copy(history = history)
    }

    override fun UpdateBuilder<Int>.toRow(item: IncomeOccurrence) {
        this[table.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[table.amount] = item.amount
        this[table.itemId] = item.itemId.toInt()
        this[table.dueDate] = item.dueDate
        this[table.every] = item.every
    }
}
