package medicare

import generics.GenericRouter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import shared.taxFetcher.Medicare

class MedicareRouter(
    basePath: String,
    medicareService: MedicareService
) : GenericRouter<Medicare, MedicareTable>(
    basePath,
    medicareService,
    MedicareResponse()
) {
    override suspend fun postQualifier(receivedItem: Medicare) =
        service.getSingle { service.table.year eq receivedItem.year }

    override fun singleEq(param: String) = service.table.year eq param.toInt()
}
