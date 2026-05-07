package co.lemuel.academy.gateway

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtFilter(
    @Value("\${academy.jwt.secret}") secret: String,
    @Value("\${academy.auth.public-paths}") private val publicPaths: List<String>,
) : GlobalFilter, Ordered {

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())
    private val matcher = AntPathMatcher()

    override fun filter(exchange: ServerWebExchange,
                         chain: GatewayFilterChain): Mono<Void> {
        val req = exchange.request
        val path = req.uri.path
        val method = req.method.name()

        // Public paths: GET 만 자유, 나머지 메서드는 인증 필요 (간단 룰)
        val isPublic = publicPaths.any { matcher.match(it, path) } && method == "GET"
        // /signup, /login, /dev/auto-login 같은 정확 경로는 모든 메서드 허용
        val isFullyPublic = listOf(
            "/api/users/signup",
            "/api/users/login",
            "/api/users/dev/auto-login",
        ).any { matcher.match(it, path) }

        if (isPublic || isFullyPublic) return chain.filter(exchange)

        val auth = req.headers.getFirst("Authorization")
        if (auth.isNullOrBlank() || !auth.startsWith("Bearer ")) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return exchange.response.setComplete()
        }
        val token = auth.removePrefix("Bearer ").trim()
        return try {
            val jws = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
            val mutated = req.mutate()
                .header("X-User-Id", jws.payload.subject)
                .header("X-User-Role", jws.payload["role"] as String)
                .build()
            chain.filter(exchange.mutate().request(mutated).build())
        } catch (e: Exception) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            exchange.response.setComplete()
        }
    }

    override fun getOrder(): Int = -1
}
