package breathOfTheWild.critter

import generics.IdTable

object CrittersTable : IdTable() {
    val critter = varchar("critter", 255)
    val effectClass = varchar("effectClass", 255).nullable()
    val boostEffect = varchar("boostEffect", 255).nullable()
    val durationIncrease = varchar("durationIncrease", 255).nullable()
}
