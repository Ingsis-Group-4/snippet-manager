package app.rule.controller

import app.rule.model.dto.UserRuleOutput
import app.rule.model.enums.RuleType
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("rule")
interface RuleControllerSpec {
    @PostMapping("default")
    @Operation(
        summary = "Create all rules for authenticated user, using their corresponding default value",
    )
    fun createDefaultRulesForUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Unit>

    @GetMapping("/all/{userId}/{ruleType}")
    @Operation(
        summary = "Get all user rules from specified type",
    )
    fun getUserRules(
        @PathVariable userId: String,
        @PathVariable ruleType: RuleType,
    ): List<UserRuleOutput>
}
