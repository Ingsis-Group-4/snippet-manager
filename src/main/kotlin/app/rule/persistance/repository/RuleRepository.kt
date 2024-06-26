package app.rule.persistance.repository

import app.rule.persistance.entity.Rule
import org.springframework.data.jpa.repository.JpaRepository

interface RuleRepository : JpaRepository<Rule, String>
