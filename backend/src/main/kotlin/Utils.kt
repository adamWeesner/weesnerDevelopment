package com.weesnerdevelopment

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

enum class Paths {
    socialSecurity, medicare, federalIncomeTax, taxWithholding
}

inline fun <reified T> String.fromJson() =
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(T::class.java).fromJson(this)