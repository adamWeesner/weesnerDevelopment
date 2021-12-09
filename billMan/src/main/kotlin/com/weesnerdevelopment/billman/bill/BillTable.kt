package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.auth.user.UserTable
import com.weesnerdevelopment.billman.color.ColorTable
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.transactions.transaction

object BillTable : UUIDTable(), GenericTable {
    val owner = reference("owner", UserTable, ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val amount = varchar("amount", 255)
    val varyingAmount = bool("varyingAmount")
    val payoffAmount = varchar("payoffAmount", 255).nullable()
    val color = reference("color", ColorTable, ReferenceOption.CASCADE)
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: BillTable.() -> T) = transaction { event() }
}