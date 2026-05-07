package co.lemuel.academy.user.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtService(
    @Value("\${academy.jwt.secret}") private val secret: String,
    @Value("\${academy.jwt.ttl-hours}") private val ttlHours: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun issue(userId: UUID, role: String): String {
        val now = Date()
        val exp = Date(now.time + ttlHours * 3600 * 1000)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role)
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact()
    }

    data class Claims(val userId: UUID, val role: String)

    fun parse(token: String): Claims {
        val jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
        val sub = jws.payload.subject
        val role = jws.payload["role"] as String
        return Claims(UUID.fromString(sub), role)
    }
}
