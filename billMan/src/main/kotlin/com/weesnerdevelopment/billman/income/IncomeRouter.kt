package income

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.billMan.Income
import com.weesnerdevelopment.shared.billMan.responses.IncomeResponse
import com.weesnerdevelopment.shared.toJson
import kotlin.reflect.full.createType

class IncomeRouter(
    override val basePath: String,
    service: IncomeService
) : BaseRouter<Income, IncomeService>(
    IncomeResponse(),
    service,
    Income::class.createType()
) {
    override fun GenericResponse<Income>.parse(): String = this.toJson()
}
