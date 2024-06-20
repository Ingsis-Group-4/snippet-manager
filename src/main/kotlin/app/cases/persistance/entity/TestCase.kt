package app.cases.persistance.entity

import app.common.persistance.entity.BaseEntity
import app.manager.persistance.entity.Snippet
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class TestCase(
    var name: String,
    @ManyToOne
    @JoinColumn(name = "snippet_id", nullable = false)
    val snippet: Snippet,
    @OneToMany(mappedBy = "testCase", cascade = [CascadeType.REMOVE])
    val inputs: List<TestCaseInput> = listOf(),
    @OneToMany(mappedBy = "testCase", cascade = [CascadeType.REMOVE])
    val expectedOutputs: List<TestCaseExpectedOutput> = listOf(),
) : BaseEntity()
