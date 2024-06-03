package app.cases.persistance.repository

import app.cases.persistance.entity.TestCase
import org.springframework.data.jpa.repository.JpaRepository

interface TestCaseRepository : JpaRepository<TestCase, String> {
    fun findAllBySnippetKey(snippetKey: String): List<TestCase>
}
