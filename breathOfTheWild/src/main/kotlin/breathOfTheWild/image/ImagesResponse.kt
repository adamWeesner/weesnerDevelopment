package breathOfTheWild.image

import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.zelda.Image

data class ImagesResponse(
    override var items: List<Image> = emptyList()
) : GenericResponse<Image>