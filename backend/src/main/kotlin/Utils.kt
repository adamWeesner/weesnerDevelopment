package com.weesnerdevelopment

import auth.InvalidUserException
import auth.InvalidUserReason
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.uri
import io.ktor.response.respond
import java.lang.reflect.ParameterizedType

enum class Paths {
    user,
    socialSecurity, medicare, federalIncomeTax, taxWithholding
}

inline fun <reified T> String.fromJson() =
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(T::class.java).fromJson(this)