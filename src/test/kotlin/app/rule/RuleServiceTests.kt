package app.rule

import app.common.TestSecurityConfig
import app.manager.model.dto.CreateSnippetInput
import app.manager.persistance.repository.SnippetRepository
import app.manager.service.ManagerService
import app.rule.model.dto.UpdateUserRuleInput
import app.rule.model.enums.RuleType
import app.rule.model.enums.RuleValueType
import app.rule.persistance.entity.Rule
import app.rule.persistance.entity.UserRule
import app.rule.persistance.repository.RuleRepository
import app.rule.persistance.repository.UserRuleRepository
import app.rule.service.RuleService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(classes = [TestSecurityConfig::class])
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RuleServiceTests {
    @Autowired
    private lateinit var ruleService: RuleService

    @Autowired
    private lateinit var userRuleRepository: UserRuleRepository

    @Autowired
    private lateinit var ruleRepository: RuleRepository

    @Autowired
    private lateinit var snippetRepository: SnippetRepository

    @Autowired
    private lateinit var managerService: ManagerService

    @Test
    @WithMockUser("create-default-rules-for-user-test-user")
    fun createDefaultRulesForUserTest() {
        val rule = Rule("Formatting 1", "default", RuleValueType.STRING, RuleType.FORMATTING)
        ruleRepository.save(rule)
        ruleService.createDefaultRulesForUser("create-default-rules-for-user-test-user")
    }

    @Test
    @WithMockUser("update-user-rules-test")
    fun updateUserRuleTest(): Unit =
        runBlocking {
            val ruleInput = Rule("Formatting 1", "default", RuleValueType.STRING, RuleType.FORMATTING)
            val rule = ruleRepository.save(ruleInput)
            val userRuleInput = UserRule("update-user-rules-test", "default", false, rule)
            val userRule = userRuleRepository.save(userRuleInput)
            val updateUserRuleInput = UpdateUserRuleInput(userRule.id!!, true, "Test")
            ruleService.updateUserRules("update-user-rules-test", listOf(updateUserRuleInput))
        }

    @Test
    @WithMockUser("update-linting-rules")
    fun toJsonStringTest(): Unit =
        runBlocking {
            val ruleInput = Rule("Linting 1", "default", RuleValueType.STRING, RuleType.LINTING)
            val rule = ruleRepository.save(ruleInput)
            val userRuleInput = UserRule("update-linting-rules-test", "default", false, rule)
            val userRule = userRuleRepository.save(userRuleInput)

            val snippet = CreateSnippetInput("Snippet", "Test", "ps")
            managerService.createSnippet(snippet, "update-linting-rules-test", "token")

            val updateUserRuleInput = UpdateUserRuleInput(userRule.id!!, true, "Test")
            ruleService.updateUserRules("update-linting-rules-test", listOf(updateUserRuleInput))
        }
}
