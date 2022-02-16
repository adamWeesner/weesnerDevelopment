package com.weesnerdevelopment.businessRules

sealed class UrlType(val value: String, val url: String)

object Testing : UrlType("testing", "http://localhost")
object Dev : UrlType("development", "http://10.0.2.2")
object Prod : UrlType("production", "http://api.weesnerdevelopment.com")
