package breathOfTheWild.image

import BaseRouter
import shared.zelda.Image
import shared.zelda.responses.ImagesResponse
import kotlin.reflect.full.createType

data class ImagesRouter(
    override val basePath: String,
    override val service: ImagesService
) : BaseRouter<Image, ImagesService>(
    ImagesResponse(),
    service,
    Image::class.createType()
)
