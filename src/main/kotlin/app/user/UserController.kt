package app.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("users")
class UserController
    @Autowired
    constructor(
        private val userService: UserService,
    ) {
        @GetMapping
        fun getAllUsers(): ResponseEntity<List<User>> {
            return ResponseEntity.ok(userService.getAllUsers())
        }
    }
