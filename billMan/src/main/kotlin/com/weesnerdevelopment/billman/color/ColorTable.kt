package com.weesnerdevelopment.billman.color

import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.transactions.transaction

object ColorTable : UUIDTable(), GenericTable {
    val red = integer("red")
    val green = integer("green")
    val blue = integer("blue")
    val alpha = integer("alpha")
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: ColorTable.() -> T) = transaction { event() }
}