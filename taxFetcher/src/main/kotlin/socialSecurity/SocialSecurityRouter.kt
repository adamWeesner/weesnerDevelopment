package socialSecurity

import generics.GenericRouter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import shared.taxFetcher.SocialSecurity

class SocialSecurityRouter(
    basePath: String,
    socialSecurityService: SocialSecurityService
) : GenericRouter<SocialSecurity, SocialSecurityTable>(
    basePath,
    socialSecurityService,
    SocialSecurityResponse()
) {
    override suspend fun postQualifier(receivedItem: SocialSecurity) =
        service.getSingle { service.table.year eq receivedItem.year }

    override fun singleEq(param: String) = service.table.year eq param.toInt()
}
