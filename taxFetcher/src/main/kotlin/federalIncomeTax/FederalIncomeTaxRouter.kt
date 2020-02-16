package federalIncomeTax

import generics.GenericRouter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class FederalIncomeTaxRouter :
    GenericRouter<FederalIncomeTax, FederalIncomeTaxesTable>(FederalIncomeTaxService(), FederalIncomeTaxResponse()) {
    override val getParamName = "year"
    override val deleteParamName = "year"

    override suspend fun postQualifier(receivedItem: FederalIncomeTax) =
        service.getSingle { service.table.year eq receivedItem.year }

    override fun deleteEq(param: String) = service.table.year eq param.toInt()
}