package breathOfTheWild.critter

import BaseService
import com.weesnerdevelopment.shared.zelda.Critter
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class CrittersService : BaseService<CrittersTable, Critter>(
    CrittersTable
) {
    override val CrittersTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = Critter(
        row[table.id],
        row[table.critter],
        row[table.effectClass],
        row[table.boostEffect],
        row[table.durationIncrease],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Critter) {
        this[table.critter] = item.critter
        this[table.effectClass] = item.effectClass
        this[table.boostEffect] = item.boostEffect
        this[table.durationIncrease] = item.durationIncrease
    }
}
