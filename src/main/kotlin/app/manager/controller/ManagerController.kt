package app.manager.controller

import app.manager.model.dto.CreateSnippetInput
import app.manager.model.dto.ShareSnippetInput
import app.manager.service.ManagerService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
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
    @PostMapping("create")
    fun createSnippet(
        @Valid @RequestBody input: CreateSnippetInput,
    ): ResponseEntity<String> {
        return ResponseEntity.ok(managerService.createSnippet(input))
    }

    @GetMapping("/{snippetKey}")
    fun getSnippet(
        @PathVariable("snippetKey") snippetKey: String,
    ): ResponseEntity<String> {
        return ResponseEntity.ok(managerService.getSnippet(snippetKey))
    }

    @GetMapping("{userId}")
    fun getSnippetsFromUserId(
        @PathVariable("userId") userId: String,
    ): ResponseEntity<String> {
        return ResponseEntity.ok(managerService.getSnippetsFromUserId(userId))
    }

    @PostMapping("share")
    fun shareSnippet(
        @Valid @RequestBody input: ShareSnippetInput,
    ): ResponseEntity<String> {
        return ResponseEntity.ok(managerService.shareSnippet(input))
    }
}
