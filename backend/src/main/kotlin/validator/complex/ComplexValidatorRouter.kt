package com.weesnerdevelopment.validator.complex

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.toJson
import kotlin.reflect.full.createType

class ComplexValidatorRouter(
    override val basePath: String,
    override val service: ComplexValidatorService
) : BaseRouter<ComplexValidatorItem, ComplexValidatorService>(
    ComplexValidatorResponse(),
    service,
    ComplexValidatorItem::class.createType()
) {
    override fun GenericResponse<ComplexValidatorItem>.parse(): String = this.toJson()
}
