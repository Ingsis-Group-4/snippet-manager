package app.cases.persistance.entity

import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
class TestCase(
    val name: String,
    val snippetKey: String,
    @OneToMany(mappedBy = "testCase")
    val inputs: List<TestCaseInput> = listOf(),
    @OneToMany(mappedBy = "testCase")
    val expectedOutputs: List<TestCaseExpectedOutput> = listOf(), // Comma separated values
) : BaseEntity()
