package medicare

import generics.GenericRouter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class MedicareRouter : GenericRouter<Medicare, MedicareTable>(MedicareService(), MedicareResponse()) {
    override val getParamName = "year"
    override val deleteParamName = "year"

    override suspend fun postQualifier(receivedItem: Medicare) =
        service.getSingle { service.table.year eq receivedItem.year }

    override fun deleteEq(param: String) = service.table.year eq param.toInt()
}