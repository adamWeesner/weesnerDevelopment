package taxWithholding

import auth.UsersService
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import shared.taxFetcher.TaxWithholding

class TaxWithholdingRouter(
    basePath: String,
    taxWithholdingService: TaxWithholdingService,
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericRouter<TaxWithholding, TaxWithholdingTable>(
    basePath,
    taxWithholdingService,
    TaxWithholdingResponse()
) {
    override suspend fun postQualifier(receivedItem: TaxWithholding) =
        service.getAll().firstOrNull {
            it.year == receivedItem.year && it.type == receivedItem.type && it.payPeriod == receivedItem.payPeriod
        }

    override fun singleEq(param: String) = service.table.year eq param.toInt()

    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: TaxWithholding,
        updatedItem: TaxWithholding
    ): TaxWithholding? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
