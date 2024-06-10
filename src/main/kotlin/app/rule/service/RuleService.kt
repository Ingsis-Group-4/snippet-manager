package app.rule.service

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
    }
