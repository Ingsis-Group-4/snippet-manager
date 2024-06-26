package app.common

import app.common.integration.auth0.Auth0Api
import app.user.User

class MockAuth0Api : Auth0Api {
    override fun getAllUsers(): List<User> {
        val user1: User = User("1", "user1")
        val user2: User = User("2", "user2")
        val user3: User = User("3", "user3")
        return listOf(user1, user2, user3)
    }

    override fun getUserById(userId: String): User {
        return User(userId, "user")
    }
}
