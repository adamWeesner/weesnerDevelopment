package breathOfTheWild.frozenFood

import BaseService
import breathOfTheWild.frozenFoodEffect.FrozenFoodEffectService
import breathOfTheWild.frozenFoodIngredients.FrozenFoodIngredientsService
import breathOfTheWild.image.ImagesService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.FrozenFood

class FrozenFoodsService(
    private val imagesService: ImagesService,
    private val frozenFoodEffectService: FrozenFoodEffectService,
    private val frozenFoodIngredientsService: FrozenFoodIngredientsService
) : BaseService<FrozenFoodsTable, FrozenFood>(
    FrozenFoodsTable
) {
    override val FrozenFoodsTable.connections: Join?
        get() = this.innerJoin(imagesService.table, {
            image
        }, {
            id
        })

    override suspend fun toItem(row: ResultRow) = FrozenFood(
        row[table.id],
        row[table.name],
        imagesService.toItem(row),
        frozenFoodEffectService.getFor(row[table.id]),
        row[table.description],
        frozenFoodIngredientsService.getFor(row[table.id]),
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: FrozenFood) {
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.description] = item.description
    }
}
