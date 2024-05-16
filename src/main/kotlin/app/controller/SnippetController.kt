package app.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.PostMapping

@RestController
@RequestMapping("/snippets")
class SnippetController {

    @GetMapping
    @ResponseStatus(OK)
    fun getSnippets(): String {
        return "All snippets"
    }

    @PostMapping
    @ResponseStatus(OK)
    fun createSnippet(): String {
        return "Snippet created"
    }
}
