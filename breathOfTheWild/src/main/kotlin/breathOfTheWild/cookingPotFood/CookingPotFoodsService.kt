package breathOfTheWild.cookingPotFood

import BaseService
import breathOfTheWild.cookingPotFoodIngredients.CookingPotFoodIngredient
import breathOfTheWild.cookingPotFoodIngredients.CookingPotFoodIngredientsService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.image.ImagesTable.src
import isNotValidId
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.CookingPotFood

class CookingPotFoodsService(
    private val imagesService: ImagesService,
    private val cookingPotFoodIngredientsService: CookingPotFoodIngredientsService
) : BaseService<CookingPotFoodsTable, CookingPotFood>(
    CookingPotFoodsTable
) {
    override val CookingPotFoodsTable.connections: Join?
        get() = this.innerJoin(imagesService.table, {
            image
        }, {
            id
        })

    override suspend fun add(item: CookingPotFood): Int? {
        var currentItem = item

        // save off the image and update the current items image to have the id if it has not been saved
        val storedImage = imagesService.get { src eq item.image.src }
        if (storedImage == null) {
            imagesService.add(item.image).also {
                if (it.isNotValidId) return it
                currentItem = currentItem.copy(image = item.image.copy(id = it))
            }
        } else {
            currentItem = currentItem.copy(image = item.image.copy(id = storedImage.id))
        }

        val savedItemId = super.add(currentItem)
        if (savedItemId.isNotValidId) return savedItemId

        // save off the ingredients
        currentItem.ingredients.forEach {
            cookingPotFoodIngredientsService.add(CookingPotFoodIngredient(null, it, savedItemId!!)).also {
                if (it.isNotValidId) return it
            }
        }

        return savedItemId
    }

    override suspend fun toItem(row: ResultRow) = CookingPotFood(
        row[table.id],
        row[table.name],
        imagesService.toItem(row),
        row[table.description],
        cookingPotFoodIngredientsService.getFor(row[table.id]),
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: CookingPotFood) {
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.description] = item.description
    }
}
