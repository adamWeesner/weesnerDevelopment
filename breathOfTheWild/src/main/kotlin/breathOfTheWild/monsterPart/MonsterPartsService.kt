package breathOfTheWild.monsterPart

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.MonsterPart

class MonsterPartsService : BaseService<MonsterPartsTable, MonsterPart>(
    MonsterPartsTable
) {
    override val MonsterPartsTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = MonsterPart(
        row[table.id],
        row[table.part],
        row[table.durationIncrease],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: MonsterPart) {
        this[table.part] = item.part
        this[table.durationIncrease] = item.durationIncrease
    }
}
