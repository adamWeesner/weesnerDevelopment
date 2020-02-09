package federalIncomeTax

import MaritalStatus
import PayPeriod
import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class FederalIncomeTaxService : GenericService<FederalIncomeTax, FederalIncomeTaxes>(FederalIncomeTaxes) {
    override suspend fun to(row: ResultRow) = FederalIncomeTax(
        id = row[FederalIncomeTaxes.id],
        year = row[FederalIncomeTaxes.year],
        maritalStatus = MaritalStatus.valueOf(row[FederalIncomeTaxes.maritalStatus]),
        payPeriod = PayPeriod.valueOf(row[FederalIncomeTaxes.payPeriod]),
        over = row[FederalIncomeTaxes.over],
        notOver = row[FederalIncomeTaxes.notOver],
        plus = row[FederalIncomeTaxes.plus],
        percent = row[FederalIncomeTaxes.percent],
        nonTaxable = row[FederalIncomeTaxes.nonTaxable],
        dateCreated = row[FederalIncomeTaxes.dateCreated],
        dateUpdated = row[FederalIncomeTaxes.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: FederalIncomeTax) {
        this[FederalIncomeTaxes.year] = item.year
        this[FederalIncomeTaxes.maritalStatus] = item.maritalStatus.name
        this[FederalIncomeTaxes.payPeriod] = item.payPeriod.name
        this[FederalIncomeTaxes.over] = item.over
        this[FederalIncomeTaxes.notOver] = item.notOver
        this[FederalIncomeTaxes.plus] = item.plus
        this[FederalIncomeTaxes.percent] = item.percent
        this[FederalIncomeTaxes.nonTaxable] = item.nonTaxable
    }
}