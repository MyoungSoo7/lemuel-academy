package co.lemuel.academy.user.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByOauthProviderAndOauthId(provider: String, oauthId: String): User?
}

interface ProgressRepository : JpaRepository<Progress, ProgressId> {
    fun findByUserId(userId: UUID): List<Progress>
}
