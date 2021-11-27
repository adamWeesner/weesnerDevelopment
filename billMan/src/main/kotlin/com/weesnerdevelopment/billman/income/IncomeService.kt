package income

import BaseService
import auth.UsersService
import com.weesnerdevelopment.billman.colors.ColorsService
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import com.weesnerdevelopment.shared.billMan.Color
import com.weesnerdevelopment.shared.billMan.Income
import diff
import history.HistoryService
import isNotValidId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class IncomeService(
    private val usersService: UsersService,
    private val colorsService: ColorsService,
    private val historyService: HistoryService
) : BaseService<IncomeTable, Income>(
    IncomeTable
) {
    override val IncomeTable.connections
        get() = this.innerJoin(usersService.table, {
            ownerId
        }, {
            uuid
        }).leftJoin(colorsService.table, {
            id
        }, {
            billId
        })

    override suspend fun update(item: Income, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
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

    override suspend fun delete(item: Income, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
        item.history?.forEach {
            historyService.delete(it) {
                historyService.table.id eq it.id!!
            }
        }

        return super.delete(item, op)
    }

    override suspend fun toItem(row: ResultRow) = Income(
        id = row[table.id],
        owner = usersService.toItemRedacted(row),
        name = row[table.name],
        amount = row[table.amount],
        varyingAmount = row[table.varyingAmount],
        color = Color(red = 255, green = 255, blue = 255, alpha = 0, dateCreated = 0, dateUpdated = 0),
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    ).let {
        val history = historyService.getFor<Income>(it.id, it.owner)

        return@let if (history == null)
            it
        else
            it.copy(history = history)
    }

    override fun UpdateBuilder<Int>.toRow(item: Income) {
        this[table.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[table.name] = item.name
        this[table.amount] = item.amount
        this[table.varyingAmount] = item.varyingAmount
    }
}
