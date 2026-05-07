package co.lemuel.academy.user.auth

import co.lemuel.academy.user.domain.Role
import co.lemuel.academy.user.domain.User
import co.lemuel.academy.user.domain.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * OAuth 스텁. 실제 운영에선 token exchange + userinfo 호출 필요.
 * 현재 구현: 클라이언트가 이미 검증한 idToken/profile 을 받아 upsert.
 * Phase 2 에서 NextAuth.js 와 통합하거나 spring-security-oauth2-client 도입.
 */
data class OauthLoginRequest(
    val sub: String,                // provider-side unique id
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
)

@RestController
@RequestMapping("/api/users/oauth")
class OAuthController(
    private val users: UserRepository,
    private val jwt: JwtService,
) {
    @PostMapping("/{provider}")
    fun login(
        @PathVariable provider: String,
        @RequestBody req: OauthLoginRequest,
    ): ResponseEntity<AuthResponse> {
        val normalized = provider.lowercase()
        if (normalized !in listOf("google", "kakao", "naver"))
            return ResponseEntity.badRequest().build()
        val existing = users.findByOauthProviderAndOauthId(normalized, req.sub)
            ?: users.findByEmail(req.email)
        val user = if (existing != null) {
            existing.oauthProvider = normalized
            existing.oauthId = req.sub
            existing.avatarUrl = req.avatarUrl ?: existing.avatarUrl
            users.save(existing)
        } else {
            users.save(User(
                email = req.email,
                oauthProvider = normalized,
                oauthId = req.sub,
                displayName = req.displayName,
                avatarUrl = req.avatarUrl,
                role = Role.STUDENT,
            ))
        }
        val token = jwt.issue(user.id!!, user.role.name)
        return ResponseEntity.ok(AuthResponse(token, user.id.toString(),
            user.role.name, user.displayName))
    }
}
