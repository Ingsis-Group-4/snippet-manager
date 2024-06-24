package app.manager.controller

import app.manager.model.dto.CreateSnippetInput
import app.manager.model.dto.GetSnippetOutput
import app.manager.model.dto.ShareSnippetInput
import app.manager.service.ManagerService
import app.run.model.dto.SnippetContent
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class ManagerController(
    @Autowired val managerService: ManagerService,
) : ManagerControllerSpec {
    @GetMapping("snippets")
    override fun getSnippetsFromUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<List<GetSnippetOutput>> {
        val userId = jwt.subject
        val token = jwt.tokenValue
        return ResponseEntity.ok(managerService.getSnippetsFromUserId(userId, token))
    }

    @PostMapping("create")
    override fun createSnippet(
        @Valid @RequestBody input: CreateSnippetInput,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<GetSnippetOutput> {
        val userId = jwt.subject
        val token = jwt.tokenValue
        return ResponseEntity.ok(managerService.createSnippet(input, userId, token))
    }

    @GetMapping("snippets/{snippetId}")
    override fun getSnippet(
        @PathVariable("snippetId") snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<GetSnippetOutput> {
        val token = jwt.tokenValue
        return try {
            ResponseEntity.ok(managerService.getSnippet(snippetId, token))
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("share")
    override fun shareSnippet(
        @Valid @RequestBody input: ShareSnippetInput,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<String> {
        val token = jwt.tokenValue
        return ResponseEntity.ok(managerService.shareSnippet(input, token))
    }

    @DeleteMapping("{snippetId}")
    override fun deleteSnippet(
        @PathVariable("snippetId") snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<String> {
        val token = jwt.tokenValue
        return ResponseEntity.ok(managerService.deleteSnippet(snippetId, token))
    }

    @PutMapping("snippets/{snippetId}")
    override fun updateSnippet(
        @PathVariable("snippetId") snippetId: String,
        @RequestBody snippetUpdateInput: SnippetContent,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<GetSnippetOutput> {
        val token = jwt.tokenValue
        return ResponseEntity.ok(managerService.updateSnippet(snippetId, snippetUpdateInput, token))
    }
}
