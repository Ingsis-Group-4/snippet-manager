package app.cases.persistance.entity

import app.common.persistance.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class TestCaseInput(
    val input: String,
    @ManyToOne()
    @JoinColumn(name = "test_case_id", nullable = false)
    val testCase: TestCase,
) : BaseEntity()
