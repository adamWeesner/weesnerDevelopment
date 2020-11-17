package breathOfTheWild.effect

import BaseRouter
import shared.zelda.Effect
import shared.zelda.responses.EffectsResponse
import kotlin.reflect.full.createType

data class EffectsRouter(
    override val basePath: String,
    override val service: EffectsService
) : BaseRouter<Effect, EffectsService>(
    EffectsResponse(),
    service,
    Effect::class.createType()
)
