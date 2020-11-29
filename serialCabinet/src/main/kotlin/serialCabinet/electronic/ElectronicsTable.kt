package serialCabinet.electronic

import auth.UsersTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption
import serialCabinet.manufacturer.ManufacturersTable

object ElectronicsTable : IdTable() {
    val name = varchar("name", 255)
    val description = text("description")
    val image = blob("image").nullable()
    val modelNumber = varchar("modelNumber", 255).nullable()
    val serialNumber = varchar("serialNumber", 255).nullable()
    val barcode = varchar("barcode", 255).nullable()
    val barcodeImage = blob("barcodeImage").nullable()
    val manufacturer = reference("manufacturerId", ManufacturersTable.id, ReferenceOption.CASCADE).nullable()
    val manufactureDate = long("manufactureDate").nullable()
    val purchaseDate = long("purchaseDate").nullable()
    val owner = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
}
