package breathOfTheWild.elixir

import BaseRouter
import shared.zelda.Elixir
import shared.zelda.responses.ElixirsResponse
import kotlin.reflect.full.createType

data class ElixirsRouter(
    override val basePath: String,
    override val service: ElixirsService
) : BaseRouter<Elixir, ElixirsService>(
    ElixirsResponse(),
    service,
    Elixir::class.createType()
)
