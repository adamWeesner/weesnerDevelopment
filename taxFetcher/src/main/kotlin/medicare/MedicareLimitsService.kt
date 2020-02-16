package medicare

import MaritalStatus
import dbQuery
import generics.GenericService
import model.ChangeType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class MedicareLimitsService : GenericService<MedicareLimit, MedicareLimitsTable>(
    MedicareLimitsTable
) {
    override suspend fun update(item: MedicareLimit, op: SqlExpressionBuilder.() -> Op<Boolean>) = item.run {
        dbQuery {
            table.update({ op() }) {
                it.assignValues(this@run)
                it[dateUpdated] = System.currentTimeMillis()
            }
        }

        getByNameAndYear(maritalStatus, year)?.also {
            onChange(ChangeType.Update, it.id ?: throw IllegalArgumentException("The id of $it was null"), it)
        }
    }

    suspend fun getByYear(year: Int) =
        dbQuery { table.select { (table.year eq year) }.mapNotNull { to(it) } }

    suspend fun getByNameAndYear(name: MaritalStatus, year: Int) =
        dbQuery {
            table.select { (table.year eq year) and (table.maritalStatus eq name.name) }.mapNotNull { to(it) }
        }.firstOrNull()


    override suspend fun to(row: ResultRow) = MedicareLimit(
        id = row[MedicareLimitsTable.id],
        year = row[MedicareLimitsTable.year],
        amount = row[MedicareLimitsTable.amount],
        maritalStatus = MaritalStatus.valueOf(row[MedicareLimitsTable.maritalStatus]),
        dateCreated = row[MedicareLimitsTable.dateCreated],
        dateUpdated = row[MedicareLimitsTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: MedicareLimit) {
        this[MedicareLimitsTable.amount] = item.amount
        this[MedicareLimitsTable.maritalStatus] = item.maritalStatus.name
        this[MedicareLimitsTable.year] = item.year
    }
}