package app.manager.controller

import app.manager.model.dto.CreateSnippetInput
import app.manager.model.dto.GetSnippetOutput
import app.manager.model.dto.ShareSnippetInput
import app.manager.model.dto.SnippetListOutput
import app.run.model.dto.SnippetContent
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RequestMapping("manager")
interface ManagerControllerSpec {
    @PostMapping
    @Operation(
        summary = "Create a new snippet",
    )
    fun createSnippet(
        @Valid @RequestBody input: CreateSnippetInput,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<GetSnippetOutput>

    @GetMapping("snippets")
    fun getSnippetsFromUser(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam("page_num") pageNum: Int,
        @RequestParam("page_size") pageSize: Int,
    ): ResponseEntity<SnippetListOutput>

    @GetMapping("snippets/{snippetId}")
    fun getSnippet(
        @PathVariable("snippetId") snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<GetSnippetOutput>

    @PostMapping("share")
    fun shareSnippet(
        @Valid @RequestBody input: ShareSnippetInput,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<String>

    @DeleteMapping("{snippetId}")
    fun deleteSnippet(
        @PathVariable("snippetId") snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<String>

    @PutMapping("snippets/{snippetId}")
    fun updateSnippet(
        @PathVariable("snippetId") snippetId: String,
        @RequestBody snippetUpdateInput: SnippetContent,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<GetSnippetOutput>
}
