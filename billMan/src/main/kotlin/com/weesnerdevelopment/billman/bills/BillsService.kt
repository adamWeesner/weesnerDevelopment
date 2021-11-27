package com.weesnerdevelopment.billman.bills

import BaseService
import auth.UsersService
import com.weesnerdevelopment.billman.billCategories.BillCategoriesService
import com.weesnerdevelopment.billman.billCategories.BillCategory
import com.weesnerdevelopment.billman.billSharedUsers.BillSharedUsers
import com.weesnerdevelopment.billman.billSharedUsers.BillSharedUsersService
import com.weesnerdevelopment.billman.colors.ColorsService
import com.weesnerdevelopment.shared.base.History
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import com.weesnerdevelopment.shared.billMan.Bill
import com.weesnerdevelopment.shared.billMan.Category
import com.weesnerdevelopment.shared.billMan.Color
import diff
import history.HistoryService
import isNotValidId
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class BillsService(
    private val sharedUsersService: BillSharedUsersService,
    private val usersService: UsersService,
    private val categoriesService: BillCategoriesService,
    private val colorsService: ColorsService,
    private val historyService: HistoryService
) : BaseService<BillsTable, Bill>(
    BillsTable
) {
    override val BillsTable.connections
        get() = this.innerJoin(usersService.table, {
            ownerId
        }, {
            uuid
        }).innerJoin(colorsService.table, {
            id
        }, {
            billId
        })

    override suspend fun add(item: Bill): Int? {
        var updatedSuccessful = super.add(item)

        if (updatedSuccessful.isNotValidId)
            return updatedSuccessful

        val addedBillId = updatedSuccessful!!

        item.sharedUsers?.forEach {
            updatedSuccessful = sharedUsersService.add(BillSharedUsers(billId = addedBillId, userId = it.uuid!!))

            if (updatedSuccessful.isNotValidId)
                return updatedSuccessful
        }
        item.categories.forEach {
            updatedSuccessful = categoriesService.add(BillCategory(billId = addedBillId, categoryId = it.id!!))
            if (updatedSuccessful.isNotValidId)
                return updatedSuccessful
        }

        updatedSuccessful = colorsService.add(addedBillId, item.color)

        if (updatedSuccessful.isNotValidId)
            return updatedSuccessful

        return addedBillId
    }

    override suspend fun update(item: Bill, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
        val oldItem = get {
            table.id eq item.id!!
        } ?: return null

        val historyDiff = oldItem.diff(item)
        val history = historyDiff.updates(item.owner)

        // filter out categories since they get messy..
        history.filter { !it.field.startsWith(Category::class.simpleName!!) }.forEach {
            val addedHistory = historyService.add(it)
            if (addedHistory.isNotValidId) return addedHistory
        }

        // add category id update to history
        history.filter {
            it.field.matches(Regex("${Category::class.simpleName} [0-9]+ id"))
        }.forEach {
            val updatedHistory = historyService.add(
                History(
                    field = "${Bill::class.simpleName} ${item.id} ${Category::class.simpleName}",
                    oldValue = it.oldValue,
                    newValue = it.newValue,
                    updatedBy = it.updatedBy
                )
            )

            if (updatedHistory.isNotValidId)
                return updatedHistory
        }

        if (history.any { it.field.contains(Color::class.simpleName!!.toRegex()) }) {
            val updatedColor = colorsService.update(item.color) { colorsService.table.id eq item.color.id!! }

            if (updatedColor.isNotValidId)
                return updatedColor
        }

        if (history.any { it.field.contains("sharedUser".toRegex()) }) {
            val deleted = sharedUsersService.deleteForBill(item.id!!)
            if (deleted.isNotValidId)
                return deleted

            item.sharedUsers?.forEach {
                val addedSharedUser = sharedUsersService.add(
                    BillSharedUsers(
                        billId = item.id!!,
                        userId = it.uuid!!
                    )
                )

                if (addedSharedUser.isNotValidId)
                    return addedSharedUser
            }
        }

        return super.update(item, op)
    }

    override suspend fun delete(item: Bill, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
        item.history?.forEach {
            historyService.delete(it) {
                historyService.table.id eq it.id!!
            }
        }

        return super.delete(item, op)
    }

    override suspend fun toItem(row: ResultRow) = Bill(
        id = row[table.id],
        owner = usersService.toItemRedacted(row),
        name = row[table.name],
        amount = row[table.amount],
        varyingAmount = row[table.varyingAmount],
        payoffAmount = row[table.payoffAmount],
        sharedUsers = sharedUsersService.getByBill(row[table.id]),
        categories = categoriesService.getByBill(row[table.id]),
        color = colorsService.toItem(row),
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    ).let {
        val history = historyService.getFor<Bill>(it.id, it.owner)

        return@let if (history == null)
            it
        else
            it.copy(history = history)
    }

    override fun UpdateBuilder<Int>.toRow(item: Bill) {
        this[table.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[table.name] = item.name
        this[table.amount] = item.amount
        this[table.varyingAmount] = item.varyingAmount
        this[table.payoffAmount] = item.payoffAmount
    }
}
