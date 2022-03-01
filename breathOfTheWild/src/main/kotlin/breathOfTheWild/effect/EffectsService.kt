package breathOfTheWild.effect

import BaseService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.image.ImagesTable.src
import com.weesnerdevelopment.shared.zelda.Effect
import isNotValidId
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class EffectsService(
    private val imagesService: ImagesService
) : BaseService<EffectsTable, Effect>(
    EffectsTable
) {
    override val EffectsTable.connections: Join?
        get() = innerJoin(imagesService.table, {
            image
        }, {
            id
        })

    override suspend fun add(item: Effect): Int? {
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

        return super.add(currentItem)
    }

    override suspend fun toItem(row: ResultRow) = Effect(
        row[table.id],
        row[table.name],
        imagesService.toItem(row),
        row[table.description],
        row[table.timeLimit],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Effect) {
        this[table.name] = item.name
        this[table.image] = item.image.id!!
        this[table.description] = item.description
        this[table.timeLimit] = item.timeLimit
    }
}
