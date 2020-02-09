package taxWithholding

import PayPeriod
import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class TaxWithholdingService : GenericService<TaxWithholding, TaxWithholdings>(TaxWithholdings) {
    override suspend fun to(row: ResultRow) = TaxWithholding(
        id = row[TaxWithholdings.id],
        year = row[TaxWithholdings.year],
        type = TaxWithholdingTypes.valueOf(row[TaxWithholdings.type]),
        payPeriod = PayPeriod.valueOf(row[TaxWithholdings.payPeriod]),
        amount = row[TaxWithholdings.amount],
        dateCreated = row[TaxWithholdings.dateCreated],
        dateUpdated = row[TaxWithholdings.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: TaxWithholding) {
        this[TaxWithholdings.year] = item.year
        this[TaxWithholdings.type] = item.type.name
        this[TaxWithholdings.payPeriod] = item.payPeriod.name
        this[TaxWithholdings.amount] = item.amount
    }
}
