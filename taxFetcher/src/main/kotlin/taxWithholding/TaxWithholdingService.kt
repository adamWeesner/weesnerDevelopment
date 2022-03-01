package taxWithholding

import com.weesnerdevelopment.shared.taxFetcher.PayPeriod
import com.weesnerdevelopment.shared.taxFetcher.TaxWithholding
import com.weesnerdevelopment.shared.taxFetcher.TaxWithholdingTypes
import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class TaxWithholdingService : GenericService<TaxWithholding, TaxWithholdingTable>(
    TaxWithholdingTable
) {
    override suspend fun to(row: ResultRow) = TaxWithholding(
        id = row[TaxWithholdingTable.id],
        year = row[TaxWithholdingTable.year],
        type = TaxWithholdingTypes.valueOf(row[TaxWithholdingTable.type]),
        payPeriod = PayPeriod.valueOf(row[TaxWithholdingTable.payPeriod]),
        amount = row[TaxWithholdingTable.amount],
        dateCreated = row[TaxWithholdingTable.dateCreated],
        dateUpdated = row[TaxWithholdingTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: TaxWithholding) {
        this[TaxWithholdingTable.year] = item.year
        this[TaxWithholdingTable.type] = item.type.name
        this[TaxWithholdingTable.payPeriod] = item.payPeriod.name
        this[TaxWithholdingTable.amount] = item.amount
    }
}
