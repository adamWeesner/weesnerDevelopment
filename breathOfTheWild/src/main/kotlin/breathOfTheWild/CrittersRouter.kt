package breathOfTheWild

import BaseRouter
import BaseService
import generics.IdTable
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.Critter
import shared.zelda.responses.CrittersResponse
import kotlin.reflect.full.createType

data class CrittersRouter(
    override val basePath: String,
    override val service: CrittersService
) : BaseRouter<Critter, CrittersService>(
    CrittersResponse(),
    service,
    Critter::class.createType()
)

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
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Critter) {
        this[table.critter] = item.critter
        this[table.effectClass] = item.effectClass
        this[table.boostEffect] = item.boostEffect
        this[table.durationIncrease] = item.durationIncrease
    }
}

object CrittersTable : IdTable() {
    val critter = varchar("critter", 255)
    val effectClass = varchar("effectClass", 255).nullable()
    val boostEffect = varchar("boostEffect", 255).nullable()
    val durationIncrease = varchar("durationIncrease", 255).nullable()
}