package socialSecurity

import generics.GenericRouter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import shared.taxFetcher.SocialSecurity

class SocialSecurityRouter : GenericRouter<SocialSecurity, SocialSecurityTable>(
    SocialSecurityService(),
    SocialSecurityResponse()
) {
    override suspend fun postQualifier(receivedItem: SocialSecurity) =
        service.getSingle { service.table.year eq receivedItem.year }

    override fun singleEq(param: String) = service.table.year eq param.toInt()
}
