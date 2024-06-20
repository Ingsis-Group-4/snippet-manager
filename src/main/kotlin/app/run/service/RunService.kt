package app.run.service

import app.common.integration.runner.SnippetRunnerApi
import app.rule.model.dto.UserRuleOutput
import app.rule.model.enums.RuleType
import app.rule.model.enums.RuleValueType
import app.rule.service.RuleService
import app.run.model.dto.SnippetContent
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RunService
    @Autowired
    constructor(
        private val runnerApi: SnippetRunnerApi,
        private val ruleService: RuleService,
        private val objectMapper: ObjectMapper,
    ) {
        fun formatSnippetWithUserRules(
            userId: String,
            snippetContent: SnippetContent,
        ): String {
            val formatRules = ruleService.getRulesForUserByType(userId, RuleType.FORMATTING)

            val ruleJsonString = stringifyRules(formatRules)

            return runnerApi.formatSnippet(
                snippetContent.content,
                ruleJsonString,
            )
        }

        fun stringifyRules(rules: List<UserRuleOutput>): String {
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
