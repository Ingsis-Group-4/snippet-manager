package app.manager.controller

import app.manager.model.dto.CreateSnippetInput
import app.manager.service.ManagerService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
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
}
