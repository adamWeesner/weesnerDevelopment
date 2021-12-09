package com.weesnerdevelopment.auth.user

import com.weesnerdevelopment.shared.auth.HashedUser
import com.weesnerdevelopment.shared.auth.User
import java.util.*

interface UserRepository {
    fun account(id: UUID): User?
    fun create(new: User): User
    fun login(user: HashedUser): User?
    fun update(updated: User): User?
    fun delete(id: UUID): Boolean
}