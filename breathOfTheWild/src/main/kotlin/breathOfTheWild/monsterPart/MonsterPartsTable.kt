package breathOfTheWild.monsterPart

import generics.IdTable

object MonsterPartsTable : IdTable() {
    val part = varchar("part", 255)
    val durationIncrease = varchar("durationIncrease", 255)
}
