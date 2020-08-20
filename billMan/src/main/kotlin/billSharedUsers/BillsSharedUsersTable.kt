package billSharedUsers

import auth.UsersTable
import bills.BillsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object BillsSharedUsersTable : IdTable() {
    val userId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
    val billId = reference("billId", BillsTable.id, ReferenceOption.CASCADE)
}
