package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.auth.user.UserTable
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.transactions.transaction

object CategoryTable : UUIDTable(), GenericTable {
    val name = varchar("name", 255).uniqueIndex()
    val owner = reference("owner", UserTable, ReferenceOption.CASCADE).nullable()
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: CategoryTable.() -> T) = transaction { event() }
}