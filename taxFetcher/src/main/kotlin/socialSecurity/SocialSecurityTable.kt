package socialSecurity

import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object SocialSecurityTable : IdTable(), HistoricTable {
    val year = integer("year").primaryKey()
    val percent = double("percent")
    val limit = integer("limit")
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
