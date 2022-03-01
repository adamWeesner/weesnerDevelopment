package com.weesnerdevelopment.validator

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import kotlin.reflect.full.createType

class ValidatorRouter(
    override val basePath: String,
    override val service: ValidatorService
) : BaseRouter<ValidatorItem, ValidatorService>(
    ValidatorResponse(),
    service,
    ValidatorItem::class.createType()
) {
    override fun GenericResponse<ValidatorItem>.parse(): String = this.toJson()
}
