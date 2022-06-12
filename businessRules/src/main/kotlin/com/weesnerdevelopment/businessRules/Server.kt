package com.weesnerdevelopment.businessRules

import io.ktor.server.application.*

interface Server {
    fun start(app: Application)
}