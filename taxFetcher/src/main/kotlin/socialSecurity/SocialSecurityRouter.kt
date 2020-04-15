package socialSecurity

import generics.GenericRouter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import taxFetcher.SocialSecurity

class SocialSecurityRouter : GenericRouter<SocialSecurity, SocialSecurityTable>(
    SocialSecurityService(),
    SocialSecurityResponse()
) {
    override val getParamName = "year"
    override val deleteParamName = "year"

    override suspend fun postQualifier(receivedItem: SocialSecurity) =
        service.getSingle { service.table.year eq receivedItem.year }

    override fun deleteEq(param: String) = service.table.year eq param.toInt()
}
