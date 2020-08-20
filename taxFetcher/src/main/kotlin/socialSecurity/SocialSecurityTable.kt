package socialSecurity

import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object SocialSecurityTable : IdTable(), HistoricTable {
    val year = integer("year").uniqueIndex()
    val percent = double("percent")
    val limit = integer("limit")
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
