package app.manager.persistance.entity

import jakarta.persistence.Entity

@Entity
data class Snippet(
    val name: String,
    val snippetKey: String,
    val language: String,
) : BaseEntity()
