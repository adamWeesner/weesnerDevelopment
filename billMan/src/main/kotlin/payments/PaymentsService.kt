package payments

import HistoryTypes
import auth.UsersService
import dbQuery
import generics.GenericService
import history.HistoryService
import model.ChangeType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.billMan.Payment

class PaymentsService(
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericService<Payment, PaymentsTable>(
    PaymentsTable
) {
    suspend fun getForOccurrence(id: Int) =
        dbQuery { table.select { (PaymentsTable.occurrenceId eq id) }.mapNotNull { to(it) } }

    suspend fun deleteForOccurrence(billId: Int) = dbQuery {
        table.select { (table.occurrenceId eq billId) }.mapNotNull { to(it).id }
    }.forEach {
        historyService.apply { getIdsFor(HistoryTypes.Payment.name, it).forEach { delete(it) { table.id eq it } } }
        delete(it) { table.id eq it }
    }

    @Deprecated(
        "Use `addForOccurrence`",
        ReplaceWith("addForOccurrence(occurrence.id, payment)"),
        DeprecationLevel.ERROR
    )
    override suspend fun add(item: Payment) = null

    suspend fun addForOccurrence(occurrenceId: Int, item: Payment): Payment? {
        var key = 0

        dbQuery {
            try {
                key = table.insert {
                    it.assignValues(item)
                    it[PaymentsTable.occurrenceId] = occurrenceId
                    it[dateCreated] = System.currentTimeMillis()
                    it[dateUpdated] = System.currentTimeMillis()
                } get table.id
            } catch (e: Throwable) {
                println("adding item threw $e")
            }
        }
        return getSingle { table.id eq key }?.also {
            onChange(ChangeType.Create, key, it)
        }
    }

    override suspend fun to(row: ResultRow) = Payment(
        id = row[PaymentsTable.id],
        owner = usersService.getUserByUuidRedacted(row[PaymentsTable.ownerId])
            ?: throw IllegalArgumentException("No user found for payment."),
        amount = row[PaymentsTable.amount],
        history = historyService.getFor(HistoryTypes.Color.name, row[PaymentsTable.id]),
        dateCreated = row[PaymentsTable.dateCreated],
        dateUpdated = row[PaymentsTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: Payment) {
        this[PaymentsTable.ownerId] = item.owner.uuid!!
        this[PaymentsTable.amount] = item.amount
    }
}
