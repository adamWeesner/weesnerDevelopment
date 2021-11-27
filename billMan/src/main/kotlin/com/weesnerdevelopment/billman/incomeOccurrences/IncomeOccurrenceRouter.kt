package incomeOccurrences

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.billMan.IncomeOccurrence
import com.weesnerdevelopment.shared.billMan.responses.IncomeOccurrencesResponse
import com.weesnerdevelopment.shared.toJson
import kotlin.reflect.full.createType

class IncomeOccurrenceRouter(
    override val basePath: String,
    service: IncomeOccurrencesService
) : BaseRouter<IncomeOccurrence, IncomeOccurrencesService>(
    IncomeOccurrencesResponse(),
    service,
    IncomeOccurrence::class.createType()
) {
    override fun GenericResponse<IncomeOccurrence>.parse(): String = this.toJson()
}
