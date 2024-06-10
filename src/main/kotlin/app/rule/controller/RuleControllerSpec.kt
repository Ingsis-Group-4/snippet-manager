package app.rule.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/rule")
interface RuleControllerSpec {
    @PostMapping("/default")
    @Operation(
        summary = "Create all rules for authenticated user, using their corresponding default value",
    )
    fun createDefaultRulesForUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Unit>
}
