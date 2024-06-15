package app.rule.service

import app.rule.model.dto.UserRuleOutput
import app.rule.model.enums.RuleType
import app.rule.persistance.entity.UserRule
import app.rule.persistance.repository.RuleRepository
import app.rule.persistance.repository.UserRuleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RuleService
    @Autowired
    constructor(
        private val ruleRepository: RuleRepository,
        private val userRuleRepository: UserRuleRepository,
    ) {
        fun createDefaultRulesForUser(userId: String) {
            val rules = ruleRepository.findAll()
            val userRuleEntities = mutableListOf<UserRule>()

            for (rule in rules) {
                userRuleEntities.add(
                    UserRule(userId, rule.defaultValue, false, rule),
                )
            }

            userRuleRepository.saveAll(userRuleEntities)
        }

        fun getRulesForUserByType(
            userId: String,
            ruleType: RuleType,
        ): List<UserRuleOutput> {
            val userRules = this.userRuleRepository.findAllByUserIdAndRule_RuleType(userId, ruleType)

            return userRules.map { toUserRuleOutput(it) }
        }

        private fun toUserRuleOutput(userRule: UserRule): UserRuleOutput {
            return UserRuleOutput(
                id = userRule.id!!,
                userId = userRule.userId,
                name = userRule.rule.name,
                isActive = userRule.isActive,
                value = userRule.value,
                valueType = userRule.rule.valueType,
                type = userRule.rule.ruleType,
            )
        }
    }
