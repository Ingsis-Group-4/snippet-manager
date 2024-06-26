package app.manager.persistance.entity

import app.common.persistance.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
data class Snippet(
    val name: String,
    val snippetKey: String,
    val language: String,
    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "snippet")
    val snippetUserStatuses: List<SnippetUserStatus> = listOf(),
) : BaseEntity()
