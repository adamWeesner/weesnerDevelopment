package breathOfTheWild.frozenFood

import BaseService
import breathOfTheWild.frozenFoodEffect.FrozenFoodEffect
import breathOfTheWild.frozenFoodEffect.FrozenFoodEffectService
import breathOfTheWild.frozenFoodIngredients.FrozenFoodIngredient
import breathOfTheWild.frozenFoodIngredients.FrozenFoodIngredientsService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.image.ImagesTable.src
import isNotValidId
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

    override suspend fun add(item: FrozenFood): Int? {
        var currentItem = item

        // save off the image and update the current items image to have the id if it has not been saved
        val storedImage = imagesService.get { src eq currentItem.image.src }
        if (storedImage == null) {
            imagesService.add(currentItem.image).also {
                if (it.isNotValidId) return it
                currentItem = currentItem.copy(image = currentItem.image.copy(id = it))
            }
        } else {
            currentItem = currentItem.copy(image = currentItem.image.copy(id = storedImage.id))
        }

        val savedItemId = super.add(currentItem)
        if (savedItemId.isNotValidId) return savedItemId

        // save off the hearts images
        currentItem.effect.forEach {
            var heartId: Int
            val heartImage = imagesService.get { src eq it.src }
            if (heartImage == null) {
                imagesService.add(currentItem.image).also {
                    if (it.isNotValidId) return it
                    heartId = it!!
                }
            } else {
                heartId = heartImage.id ?: return heartImage.id
            }

            frozenFoodEffectService.add(FrozenFoodEffect(null, heartId, savedItemId!!))
        }

        // save off the ingredients
        currentItem.ingredients.forEach {
            frozenFoodIngredientsService.add(FrozenFoodIngredient(null, it, savedItemId!!)).also {
                if (it.isNotValidId) return it
            }
        }

        return savedItemId
    }

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
