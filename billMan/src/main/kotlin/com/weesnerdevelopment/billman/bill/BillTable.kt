package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.billman.color.ColorTable
import com.weesnerdevelopment.businessRules.tryTransaction
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object BillTable : UUIDTable(), GenericTable {
    val owner = varchar("owner", 36)
    val name = varchar("name", 255)
    val amount = varchar("amount", 255)
    val varyingAmount = bool("varyingAmount")
    val payoffAmount = varchar("payoffAmount", 255).nullable()
    val color = reference("color", ColorTable, ReferenceOption.CASCADE)
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: BillTable.() -> T) = tryTransaction(event)
}