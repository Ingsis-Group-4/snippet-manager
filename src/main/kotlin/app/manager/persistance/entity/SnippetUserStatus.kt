package app.manager.persistance.entity

import app.common.persistance.entity.BaseEntity
import app.manager.model.enums.SnippetStatus
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
data class SnippetUserStatus(
    val userId: String,
    @Enumerated(EnumType.STRING)
    val status: SnippetStatus,
    @ManyToOne
    @JoinColumn(name = "snippet_id", nullable = false)
    val snippet: Snippet,
) : BaseEntity()
