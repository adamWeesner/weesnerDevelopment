package incomeOccurrences

import BaseService
import auth.UsersService
import diff
import history.HistoryService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException
import shared.billMan.Occurrence

class IncomeOccurrencesService(
    private val usersService: UsersService,
    private val historyService: HistoryService
) : BaseService<IncomeOccurrencesTable, Occurrence>(
    IncomeOccurrencesTable
) {
    private val IncomeOccurrencesTable.connections
        get() = this.innerJoin(usersService.table, {
            ownerId
        }, {
            uuid
        })

    override suspend fun getAll() = tryCall {
        table.connections.selectAll().mapNotNull {
            toItem(it)
        }
    }

    override suspend fun get(op: SqlExpressionBuilder.() -> Op<Boolean>) = tryCall {
        table.connections.select {
            op()
        }.limit(1).firstOrNull()?.let {
            toItem(it)
        }
    }

    override suspend fun update(item: Occurrence, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
        val oldItem = get {
            table.id eq item.id!!
        } ?: return null

        oldItem.diff(item).updates(item.owner).forEach {
            historyService.add(it)
        }

        return super.update(item, op)
    }

    override suspend fun toItem(row: ResultRow) = Occurrence(
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
        val history = historyService.getFor<Occurrence>(it.id, it.owner)

        return@let if (history == null)
            it
        else
            it.copy(history = history)
    }

    override fun UpdateBuilder<Int>.toRow(item: Occurrence) {
        this[table.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[table.amount] = item.amount
        this[table.itemId] = item.itemId.toInt()
        this[table.dueDate] = item.dueDate
        this[table.every] = item.every
    }
}
