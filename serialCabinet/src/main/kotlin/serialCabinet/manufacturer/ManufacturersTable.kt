package serialCabinet.manufacturer

import generics.IdTable

object ManufacturersTable : IdTable() {
    val name = varchar("name", 255)
}
