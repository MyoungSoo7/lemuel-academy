package co.lemuel.academy.user.auth

import co.lemuel.academy.user.domain.Role
import co.lemuel.academy.user.domain.User
import co.lemuel.academy.user.domain.UserRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*

data class SignupRequest(
    @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:NotBlank val displayName: String,
)

data class LoginRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String,
)

data class AuthResponse(
    val token: String,
    val userId: String,
    val role: String,
    val displayName: String,
)

@RestController
@RequestMapping("/api/users")
class AuthController(
    private val users: UserRepository,
    private val jwt: JwtService,
) {
    private val encoder = BCryptPasswordEncoder()

    /** Dev/MVP only — 역할별 데모 유저로 자동 로그인.
     *  운영 시에는 비활성화 (profile=prod 에서 disable 등). */
    @PostMapping("/dev/auto-login")
    fun autoLogin(
        @RequestParam(defaultValue = "STUDENT") role: String,
    ): ResponseEntity<AuthResponse> {
        val normalizedRole = runCatching { Role.valueOf(role.uppercase()) }
            .getOrElse { Role.STUDENT }
        val email = "demo-${normalizedRole.name.lowercase()}@academy.local"
        val user = users.findByEmail(email) ?: users.save(User(
            email = email,
            displayName = when (normalizedRole) {
                Role.STUDENT  -> "데모 학생"
                Role.CREATOR  -> "데모 크리에이터"
                Role.ADMIN    -> "데모 관리자"
            },
            role = normalizedRole,
        ))
        val token = jwt.issue(user.id!!, user.role.name)
        return ResponseEntity.ok(AuthResponse(token, user.id.toString(),
            user.role.name, user.displayName))
    }

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody req: SignupRequest): ResponseEntity<AuthResponse> {
        users.findByEmail(req.email)?.let {
            return ResponseEntity.status(409).build()
        }
        val user = User(
            email = req.email,
            passwordHash = encoder.encode(req.password),
            displayName = req.displayName,
            role = Role.STUDENT,
        )
        val saved = users.save(user)
        val token = jwt.issue(saved.id!!, saved.role.name)
        return ResponseEntity.ok(AuthResponse(token, saved.id.toString(),
            saved.role.name, saved.displayName))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        val user = users.findByEmail(req.email)
            ?: return ResponseEntity.status(401).build()
        if (user.passwordHash == null || !encoder.matches(req.password, user.passwordHash))
            return ResponseEntity.status(401).build()
        val token = jwt.issue(user.id!!, user.role.name)
        return ResponseEntity.ok(AuthResponse(token, user.id.toString(),
            user.role.name, user.displayName))
    }
}
