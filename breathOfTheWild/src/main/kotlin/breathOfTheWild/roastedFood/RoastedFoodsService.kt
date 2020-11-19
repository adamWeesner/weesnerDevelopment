package breathOfTheWild.roastedFood

import BaseService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.roastedFoodEffect.RoastedFoodEffectService
import breathOfTheWild.roastedFoodIngredients.RoastedFoodIngredientsService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.RoastedFood

class RoastedFoodsService(
    private val imagesService: ImagesService,
    private val roastedFoodEffectService: RoastedFoodEffectService,
    private val roastedFoodIngredientsService: RoastedFoodIngredientsService
) : BaseService<RoastedFoodsTable, RoastedFood>(
    RoastedFoodsTable
) {
    override val RoastedFoodsTable.connections: Join?
        get() = innerJoin(imagesService.table, {
            image
        }, {
            id
        })

    override suspend fun toItem(row: ResultRow) = RoastedFood(
        row[table.id],
        row[table.name],
        imagesService.toItem(row),
        roastedFoodEffectService.getFor(row[table.id]),
        row[table.description],
        roastedFoodIngredientsService.getFor(row[table.id]),
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: RoastedFood) {
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.description] = item.description
    }
}
