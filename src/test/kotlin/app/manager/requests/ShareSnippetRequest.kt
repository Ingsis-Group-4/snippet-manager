package app.manager.requests

import app.manager.model.dto.ShareSnippetInput

fun shareSnippetMockRequest(
    snippetId: String,
    userId: String,
): ShareSnippetInput {
    return ShareSnippetInput(
        snippetId = snippetId,
        userId = userId,
    )
}
