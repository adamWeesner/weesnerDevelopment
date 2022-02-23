package com.weesnerdevelopment.auth.user

import com.weesnerdevelopment.shared.auth.HashedUser
import com.weesnerdevelopment.shared.auth.User

interface UserRepository {
    fun info(id: String): User?
    fun account(id: String): User?
    fun create(new: User): User?
    fun login(user: HashedUser): User?
    fun update(updated: User): User?
    fun delete(id: String): Boolean
}