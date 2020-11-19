package breathOfTheWild.otherFood

import BaseService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.otherFoodEffect.OtherFoodEffectService
import breathOfTheWild.otherFoodIngredients.OtherFoodIngredientsService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.OtherFood

class OtherFoodsService(
    private val imagesService: ImagesService,
    private val otherFoodEffectService: OtherFoodEffectService,
    private val otherFoodIngredientsService: OtherFoodIngredientsService
) : BaseService<OtherFoodsTable, OtherFood>(
    OtherFoodsTable
) {
    override val OtherFoodsTable.connections: Join?
        get() = this.innerJoin(imagesService.table, {
            image
        }, {
            id
        })

    override suspend fun toItem(row: ResultRow) = OtherFood(
        row[table.id],
        row[table.name],
        imagesService.toItem(row),
        otherFoodEffectService.getFor(row[table.id]),
        row[table.description],
        otherFoodIngredientsService.getFor(row[table.id]),
        row[table.method],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: OtherFood) {
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.description] = item.description
        this[table.method] = item.method
    }
}
