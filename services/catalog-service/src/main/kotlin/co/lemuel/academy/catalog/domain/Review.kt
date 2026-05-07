package co.lemuel.academy.catalog.domain

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "reviews", schema = "catalog")
class Review(
    @Id @GeneratedValue var id: UUID? = null,
    @Column(name = "course_id", nullable = false) var courseId: UUID,
    @Column(name = "user_id", nullable = false) var userId: UUID,
    @Column(nullable = false) var rating: Short,
    @Column(columnDefinition = "TEXT") var content: String? = null,
    @Column(name = "created_at") var createdAt: OffsetDateTime = OffsetDateTime.now(),
)

interface ReviewRepository : JpaRepository<Review, UUID> {
    fun findByCourseIdOrderByCreatedAtDesc(courseId: UUID): List<Review>
    fun findByCourseIdAndUserId(courseId: UUID, userId: UUID): Review?
    @Query("SELECT AVG(r.rating), COUNT(r) FROM Review r WHERE r.courseId = :id")
    fun aggregate(id: UUID): Array<Any?>
}
