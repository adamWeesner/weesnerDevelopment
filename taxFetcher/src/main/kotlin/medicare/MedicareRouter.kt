package medicare

import auth.UsersService
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.taxFetcher.Medicare
import com.weesnerdevelopment.shared.taxFetcher.responses.MedicareResponse
import com.weesnerdevelopment.shared.toJson
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.*
import io.ktor.util.pipeline.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class MedicareRouter(
    basePath: String,
    medicareService: MedicareService,
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericRouter<Medicare, MedicareTable>(
    basePath,
    medicareService,
    MedicareResponse()
) {
    override fun GenericResponse<Medicare>.parse(): String = this.toJson()

    override suspend fun postQualifier(receivedItem: Medicare) =
        service.getSingle { service.table.year eq receivedItem.year }

    override fun singleEq(param: String) = service.table.year eq param.toInt()

    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: Medicare,
        updatedItem: Medicare
    ): Medicare? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
