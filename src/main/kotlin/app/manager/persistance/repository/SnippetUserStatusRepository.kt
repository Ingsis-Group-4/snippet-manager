package app.manager.persistance.repository

import app.manager.model.enums.SnippetStatus
import app.manager.persistance.entity.SnippetUserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface SnippetUserStatusRepository : JpaRepository<SnippetUserStatus, String> {
    fun findAllByUserId(userId: String): List<SnippetUserStatus>

    @Modifying
    @Query(
        """
            update SnippetUserStatus sus set sus.status = :newStatus 
            where sus.userId=:userId
            and sus.snippet.snippetKey=:snippetKey
        """,
    )
    fun updateByUserIdAndSnippetKey(
        userId: String,
        snippetKey: String,
        newStatus: SnippetStatus,
    )

    @Suppress("ktlint:standard:function-naming")
    fun findByUserIdAndSnippet_Id(
        userId: String,
        snippetId: String,
    ): SnippetUserStatus?
}
