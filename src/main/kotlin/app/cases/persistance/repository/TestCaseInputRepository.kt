package app.cases.persistance.repository

import app.cases.persistance.entity.TestCaseInput
import org.springframework.data.jpa.repository.JpaRepository

interface TestCaseInputRepository : JpaRepository<TestCaseInput, String>
