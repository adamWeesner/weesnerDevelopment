package breathOfTheWild.roastedFood

import BaseService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.image.ImagesTable.src
import breathOfTheWild.roastedFoodEffect.RoastedFoodEffect
import breathOfTheWild.roastedFoodEffect.RoastedFoodEffectService
import breathOfTheWild.roastedFoodIngredients.RoastedFoodIngredient
import breathOfTheWild.roastedFoodIngredients.RoastedFoodIngredientsService
import com.weesnerdevelopment.shared.zelda.RoastedFood
import isNotValidId
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder

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

    override suspend fun add(item: RoastedFood): Int? {
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

            roastedFoodEffectService.add(RoastedFoodEffect(null, heartId, savedItemId!!))
        }

        // save off the ingredients
        currentItem.ingredients.forEach {
            roastedFoodIngredientsService.add(RoastedFoodIngredient(null, it, savedItemId!!)).also {
                if (it.isNotValidId) return it
            }
        }

        return savedItemId
    }

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
