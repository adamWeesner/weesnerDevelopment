package com.weesnerdevelopment

import io.ktor.config.ApplicationConfig
import java.net.InetAddress

class AppConfig(
    private val config: ApplicationConfig
) {
    private val jwt = "ktor.jwt"
    private val deployment = "ktor.deployment"

    val appEnv = getDeployment("environment")
    val port = getDeployment("port")

    val issuer = getJwt("domain")
    val audience = getJwt("audience")
    val realm = getJwt("realm")
    val secret = getJwt("secret")
    val expiresIn = getJwt("expiresIn").toLong()

    val baseUrl get() = if (isDevelopment) getLocalIp() else "weesnerDevelopment.com"
    val isDevelopment get() = appEnv == Environment.development.name

    private fun getDeployment(item: String) = config.property("$deployment.$item").getString()
    private fun getJwt(item: String) = config.property("$jwt.$item").getString()

    private fun getLocalIp(): String = InetAddress.getLocalHost().run {
        println("Host Address - $hostAddress")
        println("Host Name - $hostName")
        hostAddress
    }
}

/**
 * Which environment we are running in.
 */
enum class Environment {
    development, production
}