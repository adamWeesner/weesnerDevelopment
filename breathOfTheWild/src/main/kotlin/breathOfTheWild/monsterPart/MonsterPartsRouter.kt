package breathOfTheWild.monsterPart

import BaseRouter
import shared.zelda.MonsterPart
import shared.zelda.responses.MonsterPartsResponse
import kotlin.reflect.full.createType

data class MonsterPartsRouter(
    override val basePath: String,
    override val service: MonsterPartsService
) : BaseRouter<MonsterPart, MonsterPartsService>(
    MonsterPartsResponse(),
    service,
    MonsterPart::class.createType()
)
