package incomeOccurrences

import auth.UsersTable
import com.weesnerdevelopment.billman.income.IncomeTable
import com.weesnerdevelopment.billman.occurrences.OccurrenceTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object IncomeOccurrencesTable : IdTable(), OccurrenceTable, HistoricTable {
    override val ownerId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
    override val amount = varchar("amount", 255)
    override val itemId = reference("itemId", IncomeTable.id, ReferenceOption.CASCADE)
    override val dueDate = long("payDate")
    override val every = varchar("every", 255)
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
