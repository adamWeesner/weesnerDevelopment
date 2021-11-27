package com.weesnerdevelopment.billman.billCategories

import BaseService
import com.weesnerdevelopment.billman.categories.CategoriesService
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class BillCategoriesService(
    private val categoriesService: CategoriesService
) : BaseService<BillCategoriesTable, BillCategory>(
    BillCategoriesTable
) {
    override val BillCategoriesTable.connections
        get() = this.innerJoin(categoriesService.table, {
            categoryId
        }, {
            id
        })

    suspend fun getByBill(id: Int) = getAll {
        table.billId eq id
    }?.mapNotNull {
        categoriesService.get { categoriesService.table.id eq it[table.categoryId] }
    } ?: throw InvalidAttributeException("Bill categories")


    override suspend fun toItem(row: ResultRow) = BillCategory(
        id = row[table.id],
        billId = row[table.billId],
        categoryId = row[table.categoryId],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: BillCategory) {
        this[table.billId] = item.billId
        this[table.categoryId] = item.categoryId
    }
}
