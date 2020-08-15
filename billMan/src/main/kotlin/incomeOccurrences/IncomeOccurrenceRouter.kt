package incomeOccurrences

import BaseRouter
import shared.billMan.Occurrence
import shared.billMan.responses.OccurrencesResponse
import kotlin.reflect.full.createType

class IncomeOccurrenceRouter(
    override val basePath: String,
    service: IncomeOccurrencesService
) : BaseRouter<Occurrence, IncomeOccurrencesService>(
    OccurrencesResponse(),
    service,
    Occurrence::class.createType()
)
