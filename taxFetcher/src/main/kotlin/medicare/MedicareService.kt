package medicare

import dbQuery
import generics.GenericServiceWChildren
import model.ChangeType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class MedicareService : GenericServiceWChildren<Medicare, Medicares>(
    Medicares
) {
    private val limitsService: MedicareLimitsService by lazy {
        childServices?.firstOrNull { it.first == "Limits" }?.second as MedicareLimitsService?
            ?: throw IllegalArgumentException("MedicareLimitsService is a required child of MedicareService")
    }

    override suspend fun getAll(): List<Medicare> = super.getAll().map {
        it.copy(limits = limitsService.getByYear(it.year))
    }

    override suspend fun getSingle(id: Int): Medicare? = super.getSingle(id)?.run {
        copy(limits = limitsService.getByYear(year))
    }

    suspend fun getByYear(year: Int) =
        dbQuery { table.select { (table.year eq year) }.mapNotNull { to(it) } }.firstOrNull()

    override suspend fun add(item: Medicare): Medicare? {
        if (getByYear(item.year) == null) {
            var key = 0

            dbQuery {
                key = (table.insert {
                    it.assignValues(item)
                    it[dateCreated] = System.currentTimeMillis()
                    it[dateUpdated] = System.currentTimeMillis()
                } get table.id)
            }

            item.limits.forEach { limitsService.add(it) }

            return getSingle(key)!!.also {
                onChange(ChangeType.Create, key, it)
            }
        }

        return null
    }

    override suspend fun update(item: Medicare): Medicare? {
        item.limits.forEach {
            if (limitsService.getByNameAndYear(it.maritalStatus, it.year)?.amount != it.amount) limitsService.update(it)
        }

        return super.update(item)
    }

    override suspend fun delete(id: Int): Boolean {
        getSingle(id)?.run {
            limitsService.getByYear(year).forEach {
                if (it.id != null) delete(it.id)
            }
        }
        return super.delete(id)
    }

    override suspend fun to(row: ResultRow) = Medicare(
        id = row[Medicares.id],
        percent = row[Medicares.percent],
        additionalPercent = row[Medicares.additionalPercent],
        year = row[Medicares.year],
        limits = limitsService.getByYear(row[Medicares.year]),
        dateCreated = row[Medicares.dateCreated],
        dateUpdated = row[Medicares.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: Medicare) {
        this[Medicares.percent] = item.percent
        this[Medicares.additionalPercent] = item.additionalPercent
        this[Medicares.year] = item.year
    }
}