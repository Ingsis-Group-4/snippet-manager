package app.common.integration.auth0

import app.user.User

interface Auth0Api {
    fun getAllUsers(): List<User>

    fun getUserById(userId: String): User
}
