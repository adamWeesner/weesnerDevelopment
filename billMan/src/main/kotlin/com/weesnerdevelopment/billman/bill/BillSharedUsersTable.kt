package com.weesnerdevelopment.billman.bill

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object BillSharedUsersTable : UUIDTable() {
    val user = varchar("owner", 36)
    val bill = reference("bill", BillTable, ReferenceOption.CASCADE)
}