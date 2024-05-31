package app.manager.persistance.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Snippet(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: String? = null,
    val userId: String,
    val name: String,
    val snippetKey: String,
    val language: String,
)
