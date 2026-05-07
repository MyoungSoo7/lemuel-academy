package co.lemuel.academy.user.auth

import co.lemuel.academy.user.domain.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class MeDto(
    val id: UUID, val email: String, val displayName: String,
    val avatarUrl: String?, val role: String,
)

data class UpdateProfileRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null,
)

@RestController
@RequestMapping("/api/users/me")
class MeController(private val users: UserRepository) {

    @GetMapping
    fun me(@RequestHeader("X-User-Id") userId: UUID): ResponseEntity<MeDto> =
        users.findById(userId).map { u ->
            ResponseEntity.ok(MeDto(u.id!!, u.email, u.displayName,
                u.avatarUrl, u.role.name))
        }.orElse(ResponseEntity.notFound().build())

    @PatchMapping
    fun update(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody req: UpdateProfileRequest,
    ): ResponseEntity<MeDto> {
        val u = users.findById(userId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        req.displayName?.let { u.displayName = it }
        req.avatarUrl?.let { u.avatarUrl = it }
        val saved = users.save(u)
        return ResponseEntity.ok(MeDto(saved.id!!, saved.email,
            saved.displayName, saved.avatarUrl, saved.role.name))
    }
}
