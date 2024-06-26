package app.user

import app.common.integration.auth0.Auth0Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService
    @Autowired
    constructor(
        private val auth0Api: Auth0Api,
    ) {
        fun getAllOtherUsers(userId: String): List<User> {
            val allUsers = auth0Api.getAllUsers()
            return allUsers.filter { it.user_id != userId }
        }

        fun getUsernameById(userId: String): String {
            val user: User = auth0Api.getUserById(userId)
            return user.name
        }
    }
