package com.weesnerdevelopment.validator

import shared.base.GenericResponse

data class ValidatorResponse(
    override var items: List<ValidatorItem>? = null
) : GenericResponse<ValidatorItem>
