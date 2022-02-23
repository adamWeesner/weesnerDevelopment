package com.weesnerdevelopment.history

import com.weesnerdevelopment.auth.user.UserTable
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.transactions.transaction

object HistoryTable : UUIDTable(), GenericTable {
    val field = varchar("field", 255)
    val oldValue = varchar("oldValue", 500).nullable()
    val newValue = varchar("newValue", 500).nullable()
    val updatedBy = reference("updatedBy", UserTable, ReferenceOption.CASCADE)
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: HistoryTable.() -> T) = transaction { event() }
}