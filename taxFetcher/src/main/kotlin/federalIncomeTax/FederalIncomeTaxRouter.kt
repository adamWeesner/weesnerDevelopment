package federalIncomeTax

import generics.GenericRouter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import shared.taxFetcher.FederalIncomeTax

class FederalIncomeTaxRouter(
    basePath: String,
    federalIncomeTaxService: FederalIncomeTaxService
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
}
