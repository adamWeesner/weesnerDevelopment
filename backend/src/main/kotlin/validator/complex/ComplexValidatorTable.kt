package com.weesnerdevelopment.validator.complex

import auth.UsersTable
import categories.CategoriesTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object ComplexValidatorTable : IdTable(), HistoricTable {
    val ownerId = varchar("ownerId", 255) references UsersTable.uuid
    val name = varchar("name", 255).uniqueIndex()
    val amount = double("amount")
    val categoryId = integer("categoryId") references CategoriesTable.id
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
