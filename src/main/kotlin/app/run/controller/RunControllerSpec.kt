package app.run.controller

import app.run.model.dto.SnippetContent
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("run")
interface RunControllerSpec {
    @PostMapping("format")
    @Operation(
        summary = "Format the snippet in the request body",
    )
    fun formatSnippet(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody snippetContent: SnippetContent,
    ): ResponseEntity<String>
}
