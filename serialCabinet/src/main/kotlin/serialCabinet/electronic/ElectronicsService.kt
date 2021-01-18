package serialCabinet.electronic

import BaseService
import auth.UsersService
import diff
import history.HistoryService
import isNotValidId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import serialCabinet.itemCategories.ItemCategories
import serialCabinet.itemCategories.ItemCategoriesService
import serialCabinet.manufacturer.ManufacturersService
import shared.base.History
import shared.base.InvalidAttributeException
import shared.billMan.Bill
import shared.billMan.Category
import shared.serialCabinet.Electronic

class ElectronicsService(
    private val manufacturersService: ManufacturersService,
    private val categoriesService: ItemCategoriesService,
    private val usersService: UsersService,
    private val historyService: HistoryService
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

    override suspend fun add(item: Electronic): Int? {
        var updatedSuccessful = super.add(item)

        if (updatedSuccessful.isNotValidId)
            return updatedSuccessful

        val addedCategoryId = updatedSuccessful!!

        item.categories.forEach {
            updatedSuccessful = categoriesService.add(ItemCategories(itemId = addedCategoryId, categoryId = it.id!!))
            if (updatedSuccessful.isNotValidId)
                return updatedSuccessful
        }

        if (updatedSuccessful.isNotValidId)
            return updatedSuccessful

        return addedCategoryId
    }

    override suspend fun update(item: Electronic, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
        val oldItem = get {
            table.id eq item.id!!
        } ?: return null

        val historyDiff = oldItem.diff(item)
        val history = historyDiff.updates(item.owner)

        // filter out categories since they get messy..
        history.filter { !it.field.startsWith(Category::class.simpleName!!) }.forEach {
            val addedHistory = historyService.add(it)
            if (addedHistory.isNotValidId) return addedHistory
        }

        // add category id update to history
        history.filter {
            it.field.matches(Regex("${Category::class.simpleName} [0-9]+ id"))
        }.forEach {
            val updatedHistory = historyService.add(
                History(
                    field = "${Bill::class.simpleName} ${item.id} ${Category::class.simpleName}",
                    oldValue = it.oldValue,
                    newValue = it.newValue,
                    updatedBy = it.updatedBy
                )
            )

            if (updatedHistory.isNotValidId)
                return updatedHistory
        }

        return super.update(item, op)
    }

    override suspend fun delete(item: Electronic, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
        item.history?.forEach {
            historyService.delete(it) {
                historyService.table.id eq it.id!!
            }
        }

        return super.delete(item, op)
    }

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
