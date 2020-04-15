package medicare

import base.GenericItem
import dbQuery
import generics.GenericService
import generics.GenericServiceWChildren
import generics.IdTable
import model.ChangeType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import taxFetcher.Medicare

class MedicareService : GenericServiceWChildren<Medicare, MedicareTable>(
    MedicareTable
) {
    private val limitsService: MedicareLimitsService by lazy {
        MedicareLimitsService().apply { childServices.add(this as GenericService<GenericItem, IdTable>) }
    }

    override suspend fun getAll(): List<Medicare> = super.getAll().map {
        it.copy(limits = limitsService.getByYear(it.year))
    }

    override suspend fun getSingle(op: SqlExpressionBuilder.() -> Op<Boolean>): Medicare? = super.getSingle(op)?.run {
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

        return getSingle { table.id eq key }!!.also {
            onChange(ChangeType.Create, key, it)
        }
    }

    override suspend fun update(item: Medicare, op: SqlExpressionBuilder.() -> Op<Boolean>): Medicare? {
        item.limits.forEach {
            if (limitsService.getByNameAndYear(it.maritalStatus, it.year)?.amount != it.amount)
                limitsService.apply {
                    update(it) { (table.year eq it.year) and (table.maritalStatus eq it.maritalStatus.name) }
                }
        }
        return super.update(item, op)
    }

    override suspend fun delete(id: Int, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
        getSingle { table.id eq id }?.run {
            limits.forEach { it.id?.let { limitsService.apply { delete(it) { table.id eq it } } } }
        }
        return super.delete(id, op)
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
