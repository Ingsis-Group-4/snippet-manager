package app.manager.controller

import app.manager.model.dto.CreateSnippetInput
import app.manager.model.dto.GetAllSnippetsOutput
import app.manager.model.dto.GetSnippetOutput
import app.manager.model.dto.ShareSnippetInput
import app.manager.service.ManagerService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("manager")
@Validated
class ManagerController(
    @Autowired val managerService: ManagerService,
) {
    @GetMapping("snippets")
    fun getSnippetsFromUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<List<GetAllSnippetsOutput>> {
        val userId = jwt.subject
        println(userId)
        return ResponseEntity.ok(managerService.getSnippetsFromUserId(userId))
    }

    @PostMapping("create")
    fun createSnippet(
        @Valid @RequestBody input: CreateSnippetInput,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<GetAllSnippetsOutput> {
        val userId = jwt.subject
        return ResponseEntity.ok(managerService.createSnippet(input, userId))
    }

    @GetMapping("snippets/{snippetId}")
    fun getSnippet(
        @PathVariable("snippetId") snippetId: String,
    ): ResponseEntity<GetSnippetOutput> {
        println("Got to getSnippet")
        return try {
            ResponseEntity.ok(managerService.getSnippet(snippetId))
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("share")
    fun shareSnippet(
        @Valid @RequestBody input: ShareSnippetInput,
    ): ResponseEntity<String> {
        return ResponseEntity.ok(managerService.shareSnippet(input))
    }

    @DeleteMapping("{snippetId}")
    fun deleteSnippet(
        @PathVariable("snippetId") snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<String> {
        return ResponseEntity.ok(managerService.deleteSnippet(snippetId))
    }
}
