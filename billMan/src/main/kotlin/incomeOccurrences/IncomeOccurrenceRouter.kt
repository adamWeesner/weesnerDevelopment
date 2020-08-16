package incomeOccurrences

import BaseRouter
import shared.billMan.IncomeOccurrence
import shared.billMan.responses.IncomeOccurrencesResponse
import kotlin.reflect.full.createType

class IncomeOccurrenceRouter(
    override val basePath: String,
    service: IncomeOccurrencesService
) : BaseRouter<IncomeOccurrence, IncomeOccurrencesService>(
    IncomeOccurrencesResponse(),
    service,
    IncomeOccurrence::class.createType()
)
