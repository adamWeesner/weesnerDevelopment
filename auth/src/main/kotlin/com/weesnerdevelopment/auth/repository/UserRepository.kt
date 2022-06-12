package com.weesnerdevelopment.auth.repository

import com.weesnerdevelopment.shared.auth.TokenUser
import com.weesnerdevelopment.shared.auth.User

interface UserRepository {
    suspend fun info(id: String): Result<User>
    suspend fun account(id: String): Result<User>
    suspend fun create(new: User): Result<TokenUser>
    suspend fun login(email: String, password: String): Result<TokenUser>
    suspend fun update(updated: TokenUser): Result<TokenUser>
    suspend fun delete(id: String): Boolean
}