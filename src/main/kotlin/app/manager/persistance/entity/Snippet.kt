package app.manager.persistance.entity

import app.common.persistance.entity.BaseEntity
import jakarta.persistence.Entity

@Entity
data class Snippet(
    val name: String,
    val snippetKey: String,
    val language: String,
) : BaseEntity()
