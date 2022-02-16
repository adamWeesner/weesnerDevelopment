package com.weesnerdevelopment.billman.bill.occurrence

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object BillOccurrenceSharedUsersTable : UUIDTable() {
    val user = varchar("owner", 36)
    val occurrence = reference("occurrence", BillOccurrenceTable, ReferenceOption.CASCADE)
}