package occurrencesSharedUsers

import auth.UsersTable
import generics.IdTable
import occurrences.BillOccurrencesTable
import org.jetbrains.exposed.sql.ReferenceOption

object OccurrenceSharedUsersTable : IdTable() {
    val userId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
    val occurrenceId = reference("occurrenceId", BillOccurrencesTable.id, ReferenceOption.CASCADE)
}
