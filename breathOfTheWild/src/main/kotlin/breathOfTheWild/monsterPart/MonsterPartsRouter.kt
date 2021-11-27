package breathOfTheWild.monsterPart

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.shared.zelda.MonsterPart
import com.weesnerdevelopment.shared.zelda.responses.MonsterPartsResponse
import kotlin.reflect.full.createType

data class MonsterPartsRouter(
    override val basePath: String,
    override val service: MonsterPartsService
) : BaseRouter<MonsterPart, MonsterPartsService>(
    MonsterPartsResponse(),
    service,
    MonsterPart::class.createType()
)
