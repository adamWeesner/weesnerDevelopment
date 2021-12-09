package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.auth.user.UserTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object BillOccurrenceSharedUsersTable : Table() {
    val user = reference("owner", UserTable, ReferenceOption.CASCADE)
    val occurrence = reference("occurrence", BillOccurrenceTable, ReferenceOption.CASCADE)
}