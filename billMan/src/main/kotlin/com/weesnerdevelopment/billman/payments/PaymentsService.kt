package payments

import BaseService
import auth.UsersService
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import com.weesnerdevelopment.shared.billMan.Payment
import history.HistoryService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

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

    suspend fun addForOccurrence(occurrence: Int, item: Payment) = tryCall {
        table.insert {
            it.toRow(item)
            it[occurrenceId] = occurrence
            it[dateCreated] = System.currentTimeMillis()
            it[dateUpdated] = System.currentTimeMillis()
        } get table.occurrenceId
    }?.let {
        tryCall {
            table.connections.select {
                table.occurrenceId eq it and (table.ownerId eq item.owner.uuid!!)
            }.mapNotNull {
                toItem(it)
            }
        }
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
