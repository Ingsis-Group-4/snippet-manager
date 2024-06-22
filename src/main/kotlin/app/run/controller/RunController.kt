package app.run.controller

import app.run.model.dto.SnippetContent
import app.run.service.RunService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.RestController

@RestController
class RunController
    @Autowired
    constructor(
        private val runService: RunService,
    ) : RunControllerSpec {
        override fun formatSnippet(
            jwt: Jwt,
            snippetContent: SnippetContent,
        ): ResponseEntity<String> {
            val userId = jwt.subject
            val result = runService.formatSnippetWithUserRules(userId, snippetContent)
            return ResponseEntity.ok(result)
        }
    }
