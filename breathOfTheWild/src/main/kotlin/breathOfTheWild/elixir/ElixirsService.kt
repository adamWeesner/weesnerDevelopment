package breathOfTheWild.elixir

import BaseService
import breathOfTheWild.elixirIngredients.ElixirIngredient
import breathOfTheWild.elixirIngredients.ElixirIngredientsService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.image.ImagesTable.src
import isNotValidId
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.zelda.Elixir

class ElixirsService(
    private val imagesService: ImagesService,
    private val elixirIngredientsService: ElixirIngredientsService
) : BaseService<ElixirsTable, Elixir>(
    ElixirsTable
) {
    override val ElixirsTable.connections: Join?
        get() = this.innerJoin(imagesService.table, {
            image
        }, {
            id
        })

    override suspend fun add(item: Elixir): Int? {
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

        // save off the ingredients
        currentItem.ingredients.forEach {
            elixirIngredientsService.add(ElixirIngredient(null, it, savedItemId!!)).also {
                if (it.isNotValidId) return it
            }
        }

        return savedItemId
    }

    override suspend fun toItem(row: ResultRow) = Elixir(
        row[table.id],
        row[table.name],
        imagesService.toItem(row),
        row[table.effect],
        row[table.description],
        elixirIngredientsService.getFor(row[table.id]),
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Elixir) {
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.effect] = item.effect
        this[table.description] = item.description
    }
}
