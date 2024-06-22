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
        fun getAllUsers(): List<User> {
            return auth0Api.getAllUsers()
        }
    }
