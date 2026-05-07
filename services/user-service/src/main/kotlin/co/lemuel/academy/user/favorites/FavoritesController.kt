package co.lemuel.academy.user.favorites

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.io.Serializable
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "favorites", schema = "users_svc")
@IdClass(FavoriteId::class)
class Favorite(
    @Id @Column(name = "user_id") var userId: UUID,
    @Id @Column(name = "course_id") var courseId: UUID,
    @Column(name = "created_at") var createdAt: OffsetDateTime = OffsetDateTime.now(),
)

data class FavoriteId(val userId: UUID = UUID.randomUUID(),
                       val courseId: UUID = UUID.randomUUID()) : Serializable

interface FavoriteRepository : JpaRepository<Favorite, FavoriteId> {
    fun findByUserId(userId: UUID): List<Favorite>
}

@RestController
@RequestMapping("/api/users/favorites")
class FavoritesController(private val repo: FavoriteRepository) {

    @GetMapping
    fun list(@RequestHeader("X-User-Id") userId: UUID): List<Map<String, Any?>> =
        repo.findByUserId(userId).map {
            mapOf("courseId" to it.courseId, "createdAt" to it.createdAt)
        }

    @PostMapping("/{courseId}")
    @Transactional
    fun add(@RequestHeader("X-User-Id") userId: UUID,
            @PathVariable courseId: UUID): Map<String, Any> {
        repo.save(Favorite(userId = userId, courseId = courseId))
        return mapOf("ok" to true)
    }

    @DeleteMapping("/{courseId}")
    @Transactional
    fun remove(@RequestHeader("X-User-Id") userId: UUID,
                @PathVariable courseId: UUID): Map<String, Any> {
        repo.deleteById(FavoriteId(userId, courseId))
        return mapOf("ok" to true)
    }
}
