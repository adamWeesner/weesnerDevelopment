package com.weesnerdevelopment.billman.billSharedUsers

import auth.UsersTable
import com.weesnerdevelopment.billman.bills.BillsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object BillsSharedUsersTable : IdTable() {
    val userId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
    val billId = reference("billId", BillsTable.id, ReferenceOption.CASCADE)
}
