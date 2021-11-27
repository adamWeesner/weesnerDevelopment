package com.weesnerdevelopment.billman.categories

import auth.UsersTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object CategoriesTable : IdTable(), HistoricTable {
    val name = varchar("name", 255).uniqueIndex()
    val ownerId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE).nullable()
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
