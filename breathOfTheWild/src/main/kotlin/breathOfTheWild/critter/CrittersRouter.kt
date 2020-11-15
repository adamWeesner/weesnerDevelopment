package breathOfTheWild.critter

import BaseRouter
import shared.zelda.Critter
import shared.zelda.responses.CrittersResponse
import kotlin.reflect.full.createType

data class CrittersRouter(
    override val basePath: String,
    override val service: CrittersService
) : BaseRouter<Critter, CrittersService>(
    CrittersResponse(),
    service,
    Critter::class.createType()
)
