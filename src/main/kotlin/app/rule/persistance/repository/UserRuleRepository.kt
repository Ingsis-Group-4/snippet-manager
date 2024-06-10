package app.rule.persistance.repository

import app.rule.model.enums.RuleType
import app.rule.persistance.entity.UserRule
import org.springframework.data.jpa.repository.JpaRepository

interface UserRuleRepository : JpaRepository<UserRule, String> {
    fun findAllByUserId(userId: String): List<UserRule>

    @Suppress("ktlint:standard:function-naming")
    fun findAllByUserIdAndRule_RuleType(
        userId: String,
        ruleType: RuleType,
    ): List<UserRule>
}
