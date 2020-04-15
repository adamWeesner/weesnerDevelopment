package taxWithholding

import generics.GenericRouter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import taxFetcher.TaxWithholding

class TaxWithholdingRouter : GenericRouter<TaxWithholding, TaxWithholdingTable>(
    TaxWithholdingService(),
    TaxWithholdingResponse()
) {
    override val getParamName = "year"
    override val deleteParamName = "year"

    override suspend fun postQualifier(receivedItem: TaxWithholding) =
        service.getSingle { service.table.year eq receivedItem.year }

    override fun deleteEq(param: String) = service.table.year eq param.toInt()
}
