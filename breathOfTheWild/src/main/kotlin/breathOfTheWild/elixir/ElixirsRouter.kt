package breathOfTheWild.elixir

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.shared.zelda.Elixir
import com.weesnerdevelopment.shared.zelda.responses.ElixirsResponse
import kotlin.reflect.full.createType

data class ElixirsRouter(
    override val basePath: String,
    override val service: ElixirsService
) : BaseRouter<Elixir, ElixirsService>(
    ElixirsResponse(),
    service,
    Elixir::class.createType()
) {
    override fun GenericResponse<Elixir>.parse(): String = this.toJson()
}
