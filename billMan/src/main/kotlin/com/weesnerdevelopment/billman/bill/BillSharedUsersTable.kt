package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.auth.user.UserTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object BillSharedUsersTable : Table() {
    val user = reference("owner", UserTable, ReferenceOption.CASCADE)
    val bill = reference("bill", BillTable, ReferenceOption.CASCADE)
}