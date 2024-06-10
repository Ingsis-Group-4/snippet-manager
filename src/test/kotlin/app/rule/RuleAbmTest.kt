package app.rule

import app.common.TestSecurityConfig
import app.rule.model.enums.RuleType
import app.rule.model.enums.RuleValueType
import app.rule.persistance.entity.Rule
import app.rule.persistance.repository.RuleRepository
import app.rule.persistance.repository.UserRuleRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [TestSecurityConfig::class])
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RuleAbmTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val userRuleRepository: UserRuleRepository,
        private var ruleRepository: RuleRepository,
    ) {
        val base = "/rule"

        @BeforeAll
        fun createRules() {
            val formattingRule =
                Rule(
                    "Formatting 1",
                    "default",
                    RuleValueType.STRING,
                    RuleType.FORMATTING,
                )
            val lintingRule =
                Rule(
                    "Linting 1",
                    "default",
                    RuleValueType.STRING,
                    RuleType.LINTING,
                )
            ruleRepository.saveAll(listOf(formattingRule, lintingRule))
        }

        @Test
        @WithMockUser(username = "test")
        fun `001 _ create default rules for user`() {
            mockMvc.perform(
                post("$base/default")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer token"),
            ).andExpect(status().isOk)

            userRuleRepository.flush()

            val userRules = userRuleRepository.findAllByUserId(TestSecurityConfig.AUTH0ID)
            Assertions.assertEquals(2, userRules.size)
        }
    }
