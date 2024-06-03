package app.cases.persistance.repository

import app.cases.persistance.entity.TestCaseExpectedOutput
import org.springframework.data.jpa.repository.JpaRepository

interface TestCaseExpectedOutputRepository : JpaRepository<TestCaseExpectedOutput, String>
