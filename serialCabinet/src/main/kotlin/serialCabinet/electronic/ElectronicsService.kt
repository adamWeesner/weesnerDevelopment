package serialCabinet.electronic

import BaseService
import auth.UsersService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import serialCabinet.itemCategories.ItemCategoriesService
import serialCabinet.manufacturer.ManufacturersService
import shared.base.InvalidAttributeException
import shared.serialCabinet.Electronic

class ElectronicsService(
    private val manufacturersService: ManufacturersService,
    private val categoriesService: ItemCategoriesService,
    private val usersService: UsersService
) : BaseService<ElectronicsTable, Electronic>(
    ElectronicsTable
) {
    override val ElectronicsTable.connections: Join?
        get() = this.innerJoin(manufacturersService.table, {
            manufacturer
        }, {
            id
        }).innerJoin(usersService.table, {
            owner
        }, {
            uuid
        })

    override suspend fun toItem(row: ResultRow) = Electronic(
        row[table.id],
        row[table.name],
        row[table.description],
        row[table.image]?.bytes,
        row[table.modelNumber],
        row[table.serialNumber],
        categoriesService.getFor(row[table.id]),
        row[table.barcode],
        row[table.barcodeImage]?.bytes,
        row[table.manufactureDate],
        manufacturersService.toItem(row),
        row[table.purchaseDate],
        usersService.toItemRedacted(row),
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Electronic) {
        this[table.name] = item.name
        this[table.description] = item.description
        this[table.image] = item.image?.let { ExposedBlob(it) }
        this[table.modelNumber] = item.modelNumber
        this[table.serialNumber] = item.serialNumber
        this[table.barcode] = item.barcode
        this[table.barcodeImage] = item.barcodeImage?.let { ExposedBlob(it) }
        this[table.manufactureDate] = item.manufactureDate
        this[table.manufacturer] = item.manufacturer?.id
        this[table.purchaseDate] = item.purchaseDate
        this[table.owner] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
    }
}
