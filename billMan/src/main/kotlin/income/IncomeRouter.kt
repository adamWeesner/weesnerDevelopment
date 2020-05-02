package income

import generics.GenericRouter
import shared.billMan.Income

class IncomeRouter(
    basePath: String,
    service: IncomeService
) : GenericRouter<Income, IncomeTable>(
    basePath,
    service,
    IncomeResponse()
)
