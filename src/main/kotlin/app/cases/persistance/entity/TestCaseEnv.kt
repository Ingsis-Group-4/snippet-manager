package app.cases.persistance.entity

import app.common.persistance.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class TestCaseEnv(
    val envKey: String,
    val envValue: String,
    @ManyToOne
    @JoinColumn(name = "test_case_id", nullable = false)
    val testCase: TestCase,
) : BaseEntity()
