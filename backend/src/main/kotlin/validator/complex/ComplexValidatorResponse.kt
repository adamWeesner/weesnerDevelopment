package com.weesnerdevelopment.validator.complex

import com.weesnerdevelopment.shared.base.GenericResponse

data class ComplexValidatorResponse(
    override var items: List<ComplexValidatorItem> = emptyList()
) : GenericResponse<ComplexValidatorItem>
