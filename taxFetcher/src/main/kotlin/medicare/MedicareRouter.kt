package medicare

import auth.UsersService
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import shared.taxFetcher.Medicare

class MedicareRouter(
    basePath: String,
    medicareService: MedicareService,
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericRouter<Medicare, MedicareTable>(
    basePath,
    medicareService,
    MedicareResponse()
) {
    override suspend fun postQualifier(receivedItem: Medicare) =
        service.getSingle { service.table.year eq receivedItem.year }

    override fun singleEq(param: String) = service.table.year eq param.toInt()

    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: Medicare,
        updatedItem: Medicare
    ): Medicare? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
