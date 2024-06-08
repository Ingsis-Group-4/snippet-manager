package app.rule.persistance.entity

import app.common.persistance.entity.BaseEntity
import app.rule.model.enums.RuleType
import app.rule.model.enums.RuleValueType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity
data class Rule(
    val name: String,
    val defaultValue: String,
    @Enumerated(EnumType.STRING)
    val valueType: RuleValueType,
    @Enumerated(EnumType.STRING)
    val ruleType: RuleType,
) : BaseEntity()
