package com.weesnerdevelopment.validator.complex

import auth.UsersTable
import categories.CategoriesTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object ComplexValidatorTable : IdTable(), HistoricTable {
    val ownerId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
    val name = varchar("name", 255).uniqueIndex()
    val amount = double("amount")
    val categoryId = reference("categoryId", CategoriesTable.id, ReferenceOption.CASCADE)
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
