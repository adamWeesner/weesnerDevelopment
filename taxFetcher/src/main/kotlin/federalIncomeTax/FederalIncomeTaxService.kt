package federalIncomeTax

import com.weesnerdevelopment.shared.taxFetcher.FederalIncomeTax
import com.weesnerdevelopment.shared.taxFetcher.MaritalStatus
import com.weesnerdevelopment.shared.taxFetcher.PayPeriod
import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class FederalIncomeTaxService : GenericService<FederalIncomeTax, FederalIncomeTaxesTable>(
    FederalIncomeTaxesTable
) {
    override suspend fun to(row: ResultRow) = FederalIncomeTax(
        id = row[FederalIncomeTaxesTable.id],
        year = row[FederalIncomeTaxesTable.year],
        maritalStatus = MaritalStatus.valueOf(row[FederalIncomeTaxesTable.maritalStatus]),
        payPeriod = PayPeriod.valueOf(row[FederalIncomeTaxesTable.payPeriod]),
        over = row[FederalIncomeTaxesTable.over],
        notOver = row[FederalIncomeTaxesTable.notOver],
        plus = row[FederalIncomeTaxesTable.plus],
        percent = row[FederalIncomeTaxesTable.percent],
        nonTaxable = row[FederalIncomeTaxesTable.nonTaxable],
        dateCreated = row[FederalIncomeTaxesTable.dateCreated],
        dateUpdated = row[FederalIncomeTaxesTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: FederalIncomeTax) {
        this[FederalIncomeTaxesTable.year] = item.year
        this[FederalIncomeTaxesTable.maritalStatus] = item.maritalStatus.name
        this[FederalIncomeTaxesTable.payPeriod] = item.payPeriod.name
        this[FederalIncomeTaxesTable.over] = item.over
        this[FederalIncomeTaxesTable.notOver] = item.notOver
        this[FederalIncomeTaxesTable.plus] = item.plus
        this[FederalIncomeTaxesTable.percent] = item.percent
        this[FederalIncomeTaxesTable.nonTaxable] = item.nonTaxable
    }
}
