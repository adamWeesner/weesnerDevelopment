package breathOfTheWild.effect

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.shared.zelda.Effect
import com.weesnerdevelopment.shared.zelda.responses.EffectsResponse
import kotlin.reflect.full.createType

data class EffectsRouter(
    override val basePath: String,
    override val service: EffectsService
) : BaseRouter<Effect, EffectsService>(
    EffectsResponse(),
    service,
    Effect::class.createType()
) {
    override fun GenericResponse<Effect>.parse(): String = this.toJson()
}
