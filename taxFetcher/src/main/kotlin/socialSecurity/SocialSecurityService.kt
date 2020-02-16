package socialSecurity

import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class SocialSecurityService : GenericService<SocialSecurity, SocialSecurityTable>(
    SocialSecurityTable
) {
    override suspend fun to(row: ResultRow) = SocialSecurity(
        id = row[SocialSecurityTable.id],
        percent = row[SocialSecurityTable.percent],
        year = row[SocialSecurityTable.year],
        limit = row[SocialSecurityTable.limit],
        dateCreated = row[SocialSecurityTable.dateCreated],
        dateUpdated = row[SocialSecurityTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: SocialSecurity) {
        this[SocialSecurityTable.percent] = item.percent
        this[SocialSecurityTable.year] = item.year
        this[SocialSecurityTable.limit] = item.limit
    }
}
