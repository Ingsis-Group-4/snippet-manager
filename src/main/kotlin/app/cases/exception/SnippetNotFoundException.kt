package app.cases.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Snippet not found")
class SnippetNotFoundException : RuntimeException()
