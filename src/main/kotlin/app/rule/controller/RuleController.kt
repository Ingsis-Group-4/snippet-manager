package app.rule.controller

import app.rule.model.dto.UpdateUserRuleInput
import app.rule.model.dto.UserRuleOutput
import app.rule.model.enums.RuleType
import app.rule.service.RuleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.RestController

@RestController
class RuleController
    @Autowired
    constructor(
        private val ruleService: RuleService,
    ) : RuleControllerSpec {
        override fun createDefaultRulesForUser(jwt: Jwt): ResponseEntity<Unit> {
            val userId = jwt.subject
            ruleService.createDefaultRulesForUser(userId)
            return ResponseEntity.ok().build()
        }

        override fun getUserRules(
            jwt: Jwt,
            ruleType: RuleType,
        ): List<UserRuleOutput> {
            return this.ruleService.getRulesForUserByType(jwt.subject, ruleType)
        }

        override suspend fun updateUserRule(
            jwt: Jwt,
            updatedRules: List<UpdateUserRuleInput>,
        ): ResponseEntity<List<UserRuleOutput>> {
            val userId = jwt.subject
            return ResponseEntity.ok(this.ruleService.updateUserRules(userId, updatedRules))
        }
    }
