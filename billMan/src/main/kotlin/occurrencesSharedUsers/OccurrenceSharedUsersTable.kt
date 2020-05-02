package occurrencesSharedUsers

import auth.UsersTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import occurrences.OccurrencesTable

object OccurrenceSharedUsersTable : IdTable(), HistoricTable {
    val userId = varchar("userId", 255) references UsersTable.uuid
    val occurrenceId = integer("occurrenceId") references OccurrencesTable.id
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
