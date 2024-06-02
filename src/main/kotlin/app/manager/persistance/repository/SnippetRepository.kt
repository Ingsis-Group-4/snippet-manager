package app.manager.persistance.repository

import app.manager.persistance.entity.Snippet
import org.springframework.data.jpa.repository.JpaRepository

interface SnippetRepository : JpaRepository<Snippet, String> {
    fun findAllByUserId(userId: String): List<Snippet>

    fun findSnippetById(id: String): Snippet?

    fun findSnippetBySnippetKey(snippetKey: String): Snippet?

    fun deleteSnippetById(id: String)
}
