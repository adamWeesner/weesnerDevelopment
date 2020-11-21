package breathOfTheWild.image

import shared.base.GenericResponse
import shared.zelda.Image

data class ImagesResponse(
    override var items: List<Image>? = null
) : GenericResponse<Image>