package app.cases.persistance.repository

import app.cases.persistance.entity.TestCase
import org.springframework.data.jpa.repository.JpaRepository

interface TestCaseRepository : JpaRepository<TestCase, String> {
    @Suppress("ktlint:standard:function-naming")
    fun findAllBySnippet_Id(snippetId: String): List<TestCase>
}
