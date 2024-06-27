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
import org.slf4j.LoggerFactory
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
        private val logger = LoggerFactory.getLogger(RuleService::class.java)

        fun createDefaultRulesForUser(userId: String) {
            logger.info("Creating default rules for user with id: $userId")
            val rules = ruleRepository.findAll()
            val userRuleEntities = mutableListOf<UserRule>()

            for (rule in rules) {
                userRuleEntities.add(
                    UserRule(userId, rule.defaultValue, false, rule),
                )
            }
            logger.info("Saving default rules for user with id: $userId")
            userRuleRepository.saveAll(userRuleEntities)
        }

        fun getRulesForUserByType(
            userId: String,
            ruleType: RuleType,
        ): List<UserRuleOutput> {
            logger.info("Getting rules for user with id: $userId and type: $ruleType")
            val userRules = this.userRuleRepository.findAllByUserIdAndRule_RuleType(userId, ruleType)

            return userRules.map {
                if (it.isActive) {
                    toUserRuleOutput(it)
                } else {
                    toDefaultRuleOutput(it)
                }
            }
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

        private fun toDefaultRuleOutput(userRule: UserRule): UserRuleOutput {
            return UserRuleOutput(
                id = userRule.id!!,
                userId = userRule.userId,
                name = userRule.rule.name,
                isActive = userRule.isActive,
                value = userRule.rule.defaultValue,
                valueType = userRule.rule.valueType,
                type = userRule.rule.ruleType,
            )
        }

        suspend fun updateUserRules(
            userId: String,
            updatedRules: List<UpdateUserRuleInput>,
        ): List<UserRuleOutput> {
            logger.info("Updating rules for user with id: $userId")
            val rulesToSave = getUserRulesToSave(updatedRules)

            logger.info("Saving updated rules for user with id: $userId")
            val savedRules = this.userRuleRepository.saveAll(rulesToSave)

            if (lintingRuleWasUpdated(savedRules)) {
                logger.info("Linting rule was updated. Publishing lint event for all snippets")
                logger.info("Updating all user snippets status to PENDING")
                val userSnippets = this.managerService.updateAllUserSnippetsStatus(userId, SnippetStatus.PENDING)
                val userSnippetKeys = userSnippets.map { it.snippet.snippetKey }
                logger.info("Publishing lint event for all snippets")
                publishLintEventForAll(userId, userSnippetKeys)
            }

            return savedRules.map { toUserRuleOutput(it) }
        }

        private fun getUserRulesToSave(updatedRules: List<UpdateUserRuleInput>): List<UserRule> {
            logger.info("Getting user rules to save")
            val userRules = mutableListOf<UserRule>()
            for (updatedRule in updatedRules) {
                val userRuleOptional = this.userRuleRepository.findById(updatedRule.id)
                if (userRuleOptional.isEmpty) {
                    logger.error("User rule ${updatedRule.id} not found")
                    throw RuntimeException("User rule ${updatedRule.id} not found")
                }

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
                logger.info("Publishing lint event for snippet with key: $snippetKey")
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
