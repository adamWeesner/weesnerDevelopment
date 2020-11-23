package breathOfTheWild.otherFood

import BaseService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.image.ImagesTable.src
import breathOfTheWild.otherFoodEffect.OtherFoodEffect
import breathOfTheWild.otherFoodEffect.OtherFoodEffectService
import breathOfTheWild.otherFoodIngredients.OtherFoodIngredient
import breathOfTheWild.otherFoodIngredients.OtherFoodIngredientsService
import isNotValidId
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

    override suspend fun add(item: OtherFood): Int? {
        var currentItem = item

        // save off the image and update the current items image to have the id if it has not been saved
        val storedImage = imagesService.get { src eq item.image.src }
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

            otherFoodEffectService.add(OtherFoodEffect(null, heartId, savedItemId!!))
        }

        // save off the ingredients
        currentItem.ingredients.forEach {
            otherFoodIngredientsService.add(OtherFoodIngredient(null, it, savedItemId!!)).also {
                if (it.isNotValidId) return it
            }
        }

        return savedItemId
    }

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
