package app.rule.service

import app.manager.model.enums.SnippetStatus
import app.manager.service.ManagerService
import app.redis.producer.LintRequestProducer
import app.rule.model.dto.UpdateUserRuleInput
import app.rule.model.dto.UserRuleOutput
import app.rule.model.enums.RuleType
import app.rule.model.enums.RuleValueType
import app.rule.persistance.entity.UserRule
import app.rule.persistance.repository.RuleRepository
import app.rule.persistance.repository.UserRuleRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import spring.mvc.redis.events.LintRequestEvent

@Service
class RuleService
    @Autowired
    constructor(
        private val ruleRepository: RuleRepository,
        private val userRuleRepository: UserRuleRepository,
        private val managerService: ManagerService,
        private val lintRequestProducer: LintRequestProducer,
        private val objectMapper: ObjectMapper,
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

        suspend fun updateUserRules(
            userId: String,
            updatedRules: List<UpdateUserRuleInput>,
        ): List<UserRuleOutput> {
            val rulesToSave = getUserRulesToSave(updatedRules)

            val savedRules = this.userRuleRepository.saveAll(rulesToSave)

            if (lintingRuleWasUpdated(savedRules)) {
                val userSnippets = this.managerService.updateAllUserSnippetsStatus(userId, SnippetStatus.PENDING)
                val userSnippetKeys = userSnippets.map { it.snippet.snippetKey }
                publishLintEventForAll(userId, userSnippetKeys)
            }

            return savedRules.map { toUserRuleOutput(it) }
        }

        private fun getUserRulesToSave(updatedRules: List<UpdateUserRuleInput>): List<UserRule> {
            val userRules = mutableListOf<UserRule>()
            for (updatedRule in updatedRules) {
                val userRuleOptional = this.userRuleRepository.findById(updatedRule.id)
                if (userRuleOptional.isEmpty) throw RuntimeException("User rule ${updatedRule.id} not found")

                val userRule = userRuleOptional.get()
                userRule.value = updatedRule.value
                userRule.isActive = updatedRule.isActive

                userRules.add(userRule)
            }

            return userRules
        }

        private fun lintingRuleWasUpdated(rules: List<UserRule>): Boolean {
            return rules.any { it.rule.ruleType == RuleType.LINTING }
        }

        private suspend fun publishLintEventForAll(
            userId: String,
            snippetKeys: List<String>,
        ) {
            val userRules = getRulesForUserByType(userId, RuleType.LINTING)
            for (snippetKey in snippetKeys) {
                this.lintRequestProducer.publishEvent(
                    LintRequestEvent(userId, snippetKey, toJsonString(userRules)),
                )
            }
        }

        private fun toJsonString(rules: List<UserRuleOutput>): String {
            val rootNode = objectMapper.createObjectNode()

            for (rule in rules) {
                when (rule.valueType) {
                    RuleValueType.STRING -> rootNode.put(rule.name, rule.value)
                    RuleValueType.INTEGER -> rootNode.put(rule.name, rule.value.toInt())
                    RuleValueType.BOOLEAN -> rootNode.put(rule.name, rule.value.toBoolean())
                }
            }

            return objectMapper.writeValueAsString(rootNode)
        }
    }
