package socialSecurity

import auth.UsersService
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import shared.taxFetcher.SocialSecurity

class SocialSecurityRouter(
    basePath: String,
    socialSecurityService: SocialSecurityService,
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericRouter<SocialSecurity, SocialSecurityTable>(
    basePath,
    socialSecurityService,
    SocialSecurityResponse()
) {
    override suspend fun postQualifier(receivedItem: SocialSecurity) =
        service.getSingle { service.table.year eq receivedItem.year }

    override fun singleEq(param: String) = service.table.year eq param.toInt()

    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: SocialSecurity,
        updatedItem: SocialSecurity
    ): SocialSecurity? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
