package app.common

import app.user.User
import app.user.UserService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(classes = [TestSecurityConfig::class])
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserServiceTests {
    @Autowired
    private lateinit var userService: UserService

    @Test
    fun `get all users`() {
        val users: List<User> = userService.getAllOtherUsers("1")
        assert(users.isNotEmpty())
        assert(users.size == 2)
        assert(users[0].user_id == "2")
        assert(users[1].user_id == "3")
    }

    @Test
    fun `get username by id`() {
        val username: String = userService.getUsernameById("1")
        assert(username.isNotEmpty())
        assert(username == "user")
    }
}
