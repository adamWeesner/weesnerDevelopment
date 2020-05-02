package taxWithholding

import generics.GenericRouter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import shared.taxFetcher.TaxWithholding

class TaxWithholdingRouter(
    basePath: String,
    taxWithholdingService: TaxWithholdingService
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
}
