package occurrencesSharedUsers

import auth.UsersTable
import generics.IdTable
import occurrences.OccurrencesTable

object OccurrenceSharedUsersTable : IdTable() {
    val userId = varchar("userId", 255) references UsersTable.uuid
    val occurrenceId = integer("occurrenceId") references OccurrencesTable.id
}