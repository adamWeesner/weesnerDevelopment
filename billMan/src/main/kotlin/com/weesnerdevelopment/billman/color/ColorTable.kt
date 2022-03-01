package com.weesnerdevelopment.billman.color

import com.weesnerdevelopment.businessRules.tryTransaction
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable

object ColorTable : UUIDTable(), GenericTable {
    val red = integer("red")
    val green = integer("green")
    val blue = integer("blue")
    val alpha = integer("alpha")
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: ColorTable.() -> T) = tryTransaction(event)
}