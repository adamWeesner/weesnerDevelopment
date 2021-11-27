package com.weesnerdevelopment.billman.occurrencesSharedUsers

import auth.UsersTable
import com.weesnerdevelopment.billman.occurrences.BillOccurrencesTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object OccurrenceSharedUsersTable : IdTable() {
    val userId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
    val occurrenceId = reference("occurrenceId", BillOccurrencesTable.id, ReferenceOption.CASCADE)
}
