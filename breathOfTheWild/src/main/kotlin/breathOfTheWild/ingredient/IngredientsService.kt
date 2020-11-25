package breathOfTheWild.ingredient

import BaseService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.image.ImagesTable.src
import breathOfTheWild.ingredientBonusAddOns.IngredientBonusAddOn
import breathOfTheWild.ingredientBonusAddOns.IngredientBonusAddOnsService
import breathOfTheWild.ingredientDuration.IngredientDuration
import breathOfTheWild.ingredientDuration.IngredientDurationService
import breathOfTheWild.ingredientHearts.IngredientHeart
import breathOfTheWild.ingredientHearts.IngredientHeartsService
import isNotValidId
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.Ingredient

class IngredientsService(
    private val imagesService: ImagesService,
    private val ingredientHeartsService: IngredientHeartsService,
    private val ingredientBonusAddOnsService: IngredientBonusAddOnsService,
    private val ingredientDurationService: IngredientDurationService
) : BaseService<IngredientsTable, Ingredient>(
    IngredientsTable
) {
    override val IngredientsTable.connections: Join?
        get() = innerJoin(imagesService.table, {
            image
        }, {
            id
        })

    override suspend fun add(item: Ingredient): Int? {
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
        currentItem.hearts?.forEach {
            var heartId: Int
            val heartImage = imagesService.get { src eq it.src }
            if (heartImage == null) {
                imagesService.add(it).also {
                    if (it.isNotValidId) return it
                    heartId = it!!
                }
            } else {
                heartId = heartImage.id ?: return heartImage.id
            }

            ingredientHeartsService.add(IngredientHeart(null, heartId, savedItemId!!)).also {
                if (it.isNotValidId) return it
            }
        }

        // save off the bonus addons
        currentItem.bonusAddOns?.forEach {
            var heartId: Int
            val heartImage = imagesService.get { src eq it.src }
            if (heartImage == null) {
                imagesService.add(it).also {
                    if (it.isNotValidId) return it
                    heartId = it!!
                }
            } else {
                heartId = heartImage.id ?: return heartImage.id
            }

            ingredientBonusAddOnsService.add(IngredientBonusAddOn(null, heartId, savedItemId!!)).also {
                if (it.isNotValidId) return it
            }
        }

        // save off the durations
        currentItem.duration?.forEach {
            ingredientDurationService.add(IngredientDuration(null, it, savedItemId!!)).also {
                if (it.isNotValidId) return it
            }
        }

        return savedItemId
    }

    override suspend fun toItem(row: ResultRow) = Ingredient(
        row[table.id],
        row[table.title],
        row[table.subtitle],
        row[table.name],
        imagesService.toItem(row),
        ingredientHeartsService.getFor(row[table.id]),
        row[table.effects],
        row[table.bonus],
        ingredientBonusAddOnsService.getFor(row[table.id]),
        ingredientDurationService.getFor(row[table.id]),
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Ingredient) {
        this[table.title] = item.title
        this[table.subtitle] = item.subtitle
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.effects] = item.effects
        this[table.bonus] = item.bonus
    }
}
