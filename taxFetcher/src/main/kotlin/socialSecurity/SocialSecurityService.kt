package socialSecurity

import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class SocialSecurityService : GenericService<SocialSecurity, SocialSecuritys>(
    SocialSecuritys
) {
    override suspend fun to(row: ResultRow) = SocialSecurity(
        id = row[SocialSecuritys.id],
        percent = row[SocialSecuritys.percent],
        year = row[SocialSecuritys.year],
        limit = row[SocialSecuritys.limit],
        dateCreated = row[SocialSecuritys.dateCreated],
        dateUpdated = row[SocialSecuritys.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: SocialSecurity) {
        this[SocialSecuritys.percent] = item.percent
        this[SocialSecuritys.year] = item.year
        this[SocialSecuritys.limit] = item.limit
    }
}