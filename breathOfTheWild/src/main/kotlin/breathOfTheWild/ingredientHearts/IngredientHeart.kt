package breathOfTheWild.ingredientHearts

import com.weesnerdevelopment.shared.base.GenericItem
import com.weesnerdevelopment.shared.currentTimeMillis

data class IngredientHeart(
    override val id: Int?,
    val imageId: Int,
    val itemId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
