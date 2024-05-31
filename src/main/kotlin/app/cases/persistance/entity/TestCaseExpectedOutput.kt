package app.cases.persistance.entity

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class TestCaseExpectedOutput(
    val output: String,
    @ManyToOne()
    @JoinColumn(name = "test_case_id", nullable = false)
    val testCase: TestCase,
) : BaseEntity()
