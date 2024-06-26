package app.rule.persistance.entity

import app.common.persistance.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
data class UserRule(
    val userId: String,
    @Column(name = "rule_value")
    var value: String,
    var isActive: Boolean,
    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    val rule: Rule,
) : BaseEntity()
