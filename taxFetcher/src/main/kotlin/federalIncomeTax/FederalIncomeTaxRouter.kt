package federalIncomeTax

import auth.UsersService
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.taxFetcher.FederalIncomeTax
import com.weesnerdevelopment.shared.taxFetcher.responses.FederalIncomeTaxResponse
import com.weesnerdevelopment.shared.toJson
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.*
import io.ktor.util.pipeline.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

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
    override fun GenericResponse<FederalIncomeTax>.parse(): String = this.toJson()

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
