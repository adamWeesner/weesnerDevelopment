package com.weesnerdevelopment.validator

import BaseRouter
import kotlin.reflect.full.createType

class ValidatorRouter(
    override val basePath: String,
    override val service: ValidatorService
) : BaseRouter<ValidatorItem, ValidatorService>(
    ValidatorResponse(),
    service,
    ValidatorItem::class.createType()
)
