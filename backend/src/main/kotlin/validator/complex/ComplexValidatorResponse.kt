package com.weesnerdevelopment.validator.complex

import shared.base.GenericResponse

data class ComplexValidatorResponse(
    override var items: List<ComplexValidatorItem>? = null
) : GenericResponse<ComplexValidatorItem>
