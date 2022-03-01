package com.weesnerdevelopment.auth.user

import com.weesnerdevelopment.businessRules.tryTransaction
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable

object UserTable : UUIDTable(), GenericTable {
    val name = varchar("name", 255).nullable()
    val email = varchar("email", 255).uniqueIndex().nullable()
    val photoUrl = varchar("photoUrl", 255).nullable()
    val username = varchar("username", 255).uniqueIndex()
    val password = varchar("password", 255)
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: UserTable.() -> T) = tryTransaction(event)
}