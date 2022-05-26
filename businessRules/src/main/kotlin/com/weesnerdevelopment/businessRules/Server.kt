package com.weesnerdevelopment.businessRules

import io.ktor.application.*

interface Server {
    fun start(app: Application)
}