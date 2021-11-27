package breathOfTheWild.image

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.shared.zelda.Image
import kotlin.reflect.full.createType

data class ImagesRouter(
    override val basePath: String,
    override val service: ImagesService
) : BaseRouter<Image, ImagesService>(
    ImagesResponse(),
    service,
    Image::class.createType()
)
