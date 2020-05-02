package billSharedUsers

import auth.UsersTable
import bills.BillsTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object BillsSharedUsersTable : IdTable(), HistoricTable {
    val userId = varchar("userId", 255) references UsersTable.uuid
    val billId = integer("billId") references BillsTable.id
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
