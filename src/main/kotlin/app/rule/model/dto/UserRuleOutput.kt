package app.rule.model.dto

import app.rule.model.enums.RuleType
import app.rule.model.enums.RuleValueType

class UserRuleOutput(
    val id: String,
    val userId: String,
    val name: String,
    val value: String,
    val type: RuleType,
    val valueType: RuleValueType,
)
