package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.businessRules.tryTransaction
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable

object CategoryTable : UUIDTable(), GenericTable {
    val name = varchar("name", 255).uniqueIndex()
    val owner = varchar("owner", 36).nullable()
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: CategoryTable.() -> T) = tryTransaction(event)
}