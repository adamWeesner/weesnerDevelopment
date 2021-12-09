package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.billman.category.CategoryTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object BillsCategoriesTable : Table() {
    val category = reference("category", CategoryTable, ReferenceOption.CASCADE)
    val bill = reference("bill", BillTable, ReferenceOption.CASCADE)
}