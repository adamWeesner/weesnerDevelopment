package com.weesnerdevelopment

import com.weesnerdevelopment.service.DatabaseServer
import io.ktor.application.Application

fun Application.main() = DatabaseServer.apply { main() }
