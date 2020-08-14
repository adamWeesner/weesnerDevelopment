package com.weesnerdevelopment.validator.complex

import BaseService
import auth.UsersService
import categories.CategoriesService
import diff
import history.HistoryService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import parse
import shared.base.History
import shared.base.InvalidAttributeException
import shared.billMan.Category

class ComplexValidatorService(
    private val usersService: UsersService,
    private val categoriesService: CategoriesService,
    private val historyService: HistoryService,
    override val table: ComplexValidatorTable = ComplexValidatorTable
) : BaseService<ComplexValidatorTable, ComplexValidatorItem>(
    table
) {
    private val ComplexValidatorTable.connections
        get() = this
            .innerJoin(categoriesService.table, { categoriesService.table.id }, { categoryId })
            .innerJoin(usersService.table, { usersService.table.uuid }, { ownerId })

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

    override suspend fun update(item: ComplexValidatorItem, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
        val oldItem = get {
            table.id eq item.id!!
        } ?: return null

        oldItem.diff(item).updates(item.owner).apply {
            val addingHistory = filter {
                !it.field.startsWith(Category::class.simpleName!!)
            }.map {
                historyService.add(it)
            }

            addingHistory.forEach {
                if (it == null || it == -1)
                    return it
            }

            val categoryHistory = filter {
                it.field.matches(Regex("${Category::class.simpleName} [0-9]+ id"))
            }.map {
                historyService.add(
                    History(
                        field = "${ComplexValidatorItem::class.simpleName} ${item.id} ${Category::class.simpleName}",
                        oldValue = it.oldValue,
                        newValue = it.newValue,
                        updatedBy = it.updatedBy
                    )
                )
            }

            categoryHistory.forEach {
                if (it == null || it == -1) return it
            }
        }

        return super.update(item, op)
    }

    override suspend fun delete(item: ComplexValidatorItem, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
        item.history?.forEach {
            historyService.delete(it) {
                historyService.table.id eq it.id!!
            }
        }

        return super.delete(item, op)
    }

    override suspend fun toItem(row: ResultRow) =
        ComplexValidatorItem(
            id = row[table.id],
            owner = usersService.toItem(row).redacted().parse(),
            name = row[ComplexValidatorTable.name],
            amount = row[ComplexValidatorTable.amount],
            category = categoriesService.toItem(row),
            history = historyService.getFor(
                ComplexValidatorItem::class.simpleName,
                row[table.id],
                usersService.toItem(row).redacted().parse()
            ),
            dateCreated = row[table.dateCreated],
            dateUpdated = row[table.dateUpdated]
        )

    override fun UpdateBuilder<Int>.toRow(item: ComplexValidatorItem) {
        this[ComplexValidatorTable.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[ComplexValidatorTable.name] = item.name
        this[ComplexValidatorTable.amount] = item.amount
        this[ComplexValidatorTable.categoryId] = item.category.id ?: throw InvalidAttributeException("Category id")
    }
}
