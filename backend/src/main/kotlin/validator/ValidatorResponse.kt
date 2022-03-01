package com.weesnerdevelopment.validator

import com.weesnerdevelopment.shared.base.GenericResponse

data class ValidatorResponse(
    override var items: List<ValidatorItem> = emptyList()
) : GenericResponse<ValidatorItem>
