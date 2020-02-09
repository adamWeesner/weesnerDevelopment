package medicare

import MaritalStatus
import dbQuery
import generics.GenericService
import model.ChangeType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update

class MedicareLimitsService : GenericService<MedicareLimit, MedicareLimits>(
    MedicareLimits
) {
    override suspend fun update(item: MedicareLimit) = item.run {
        dbQuery {
            table.update({ (table.year eq year) and (table.maritalStatus eq maritalStatus.name) }) {
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
        id = row[MedicareLimits.id],
        year = row[MedicareLimits.year],
        amount = row[MedicareLimits.amount],
        maritalStatus = MaritalStatus.valueOf(row[MedicareLimits.maritalStatus]),
        dateCreated = row[MedicareLimits.dateCreated],
        dateUpdated = row[MedicareLimits.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: MedicareLimit) {
        this[MedicareLimits.amount] = item.amount
        this[MedicareLimits.maritalStatus] = item.maritalStatus.name
        this[MedicareLimits.year] = item.year
    }
}