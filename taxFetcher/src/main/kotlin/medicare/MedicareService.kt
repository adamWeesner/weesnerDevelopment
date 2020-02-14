package medicare

import dbQuery
import generics.GenericItem
import generics.GenericService
import generics.GenericServiceWChildren
import generics.IdTable
import model.ChangeType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class MedicareService : GenericServiceWChildren<Medicare, MedicareTable>(
    MedicareTable
) {
    private val limitsService: MedicareLimitsService by lazy {
        MedicareLimitsService().apply { childServices.add(this as GenericService<GenericItem, IdTable>) }
    }

    override suspend fun getAll(): List<Medicare> = super.getAll().map {
        it.copy(limits = limitsService.getByYear(it.year))
    }

    override suspend fun getSingle(id: Int): Medicare? = super.getSingle(id)?.run {
        copy(limits = limitsService.getByYear(year))
    }

    private suspend fun getByYear(year: Int) =
        dbQuery { table.select { (table.year eq year) }.mapNotNull { to(it) } }.firstOrNull()

    override suspend fun add(item: Medicare): Medicare? {
        if (getByYear(item.year) != null) return null

        val key = dbQuery {
            (table.insert {
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

    override suspend fun update(item: Medicare): Medicare? {
        item.limits.forEach {
            if (limitsService.getByNameAndYear(it.maritalStatus, it.year)?.amount != it.amount) limitsService.update(it)
        }

        return super.update(item)
    }

    override suspend fun delete(id: Int): Boolean {
        getSingle(id)?.run {
            limits.forEach {
                if (it.id != null) limitsService.delete(it.id)
            }
        }
        return super.delete(id)
    }

    override suspend fun to(row: ResultRow) = Medicare(
        id = row[MedicareTable.id],
        percent = row[MedicareTable.percent],
        additionalPercent = row[MedicareTable.additionalPercent],
        year = row[MedicareTable.year],
        limits = limitsService.getByYear(row[MedicareTable.year]),
        dateCreated = row[MedicareTable.dateCreated],
        dateUpdated = row[MedicareTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: Medicare) {
        this[MedicareTable.percent] = item.percent
        this[MedicareTable.additionalPercent] = item.additionalPercent
        this[MedicareTable.year] = item.year
    }
}