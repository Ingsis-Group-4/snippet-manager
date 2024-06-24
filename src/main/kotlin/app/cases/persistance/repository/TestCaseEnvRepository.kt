package app.cases.persistance.repository

import app.cases.persistance.entity.TestCaseEnv
import org.springframework.data.jpa.repository.JpaRepository

interface TestCaseEnvRepository : JpaRepository<TestCaseEnv, String>
