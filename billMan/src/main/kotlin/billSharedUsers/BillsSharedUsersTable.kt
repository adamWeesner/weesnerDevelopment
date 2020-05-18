package billSharedUsers

import auth.UsersTable
import bills.BillsTable
import generics.IdTable

object BillsSharedUsersTable : IdTable() {
    val userId = varchar("userId", 255) references UsersTable.uuid
    val billId = integer("billId") references BillsTable.id
}
