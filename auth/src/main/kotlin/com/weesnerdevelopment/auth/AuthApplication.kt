package com.weesnerdevelopment.auth

import io.ktor.application.*

fun Application.main() = AuthServer.apply { main() }
