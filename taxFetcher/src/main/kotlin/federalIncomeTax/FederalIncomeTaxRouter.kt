package federalIncomeTax

import auth.UsersService
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import shared.taxFetcher.FederalIncomeTax
import shared.taxFetcher.responses.FederalIncomeTaxResponse

class FederalIncomeTaxRouter(
    basePath: String,
    federalIncomeTaxService: FederalIncomeTaxService,
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericRouter<FederalIncomeTax, FederalIncomeTaxesTable>(
    basePath,
    federalIncomeTaxService,
    FederalIncomeTaxResponse()
) {
    override suspend fun postQualifier(receivedItem: FederalIncomeTax) =
        service.getAll().filter {
            it.year == receivedItem.year && it.maritalStatus == receivedItem.maritalStatus && it.payPeriod == receivedItem.payPeriod
        }.run {
            forEach {
                receivedItem.apply {
                    if ((notOver in it.over..it.notOver) || (over in it.over..it.notOver)) return@run it
                }
            }

            null
        }

    override fun singleEq(param: String) = service.table.year eq param.toInt()

    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: FederalIncomeTax,
        updatedItem: FederalIncomeTax
    ): FederalIncomeTax? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
