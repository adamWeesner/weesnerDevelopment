package income

import BaseRouter
import shared.billMan.Income
import shared.billMan.responses.IncomeResponse
import kotlin.reflect.full.createType

class IncomeRouter(
    override val basePath: String,
    service: IncomeService
) : BaseRouter<Income, IncomeService>(
    IncomeResponse(),
    service,
    Income::class.createType()
)
