package payments

import BaseService
import auth.UsersService
import history.HistoryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException
import shared.billMan.Payment

class PaymentsService(
    private val usersService: UsersService,
    private val historyService: HistoryService
) : BaseService<PaymentsTable, Payment>(
    PaymentsTable
) {
    override val PaymentsTable.connections
        get() = this.innerJoin(usersService.table, {
            ownerId
        }, {
            uuid
        })

    suspend fun getForOccurrence(id: Int) = getAll {
        table.occurrenceId eq id
    }?.mapNotNull {
        toItem(it)
    }

    @Deprecated(
        "Use `addForOccurrence`",
        ReplaceWith("addForOccurrence(occurrence.id, payment)"),
        DeprecationLevel.ERROR
    )
    override suspend fun add(item: Payment) = null

    suspend fun addForOccurrence(occurrenceId: Int, item: Payment) = tryCall {
        table.insert {
            it.toRow(item)
            it[table.occurrenceId] = occurrenceId
            it[dateCreated] = System.currentTimeMillis()
            it[dateUpdated] = System.currentTimeMillis()
        } get table.id
    }?.let {
        getForOccurrence(occurrenceId)
    }

    override suspend fun toItem(row: ResultRow) = Payment(
        id = row[table.id],
        owner = usersService.toItemRedacted(row),
        amount = row[table.amount],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    ).let {
        val history = historyService.getFor<Payment>(it.id, it.owner)

        return@let if (history == null)
            it
        else
            it.copy(history = history)
    }

    override fun UpdateBuilder<Int>.toRow(item: Payment) {
        this[table.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[table.amount] = item.amount
    }
}
