package app.manager.exceptions

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Snippet not found")
class NotFoundException(message: String) : Exception(message)
