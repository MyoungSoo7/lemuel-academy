package co.lemuel.academy.catalog.api

import co.lemuel.academy.catalog.domain.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

data class ReviewRequest(
    @field:Min(1) @field:Max(5) val rating: Short,
    val content: String? = null,
)

data class ReviewDto(
    val id: UUID, val userId: UUID, val rating: Short,
    val content: String?, val createdAt: String,
)

@RestController
@RequestMapping("/api/catalog/courses/{courseId}/reviews")
class ReviewController(
    private val courses: CourseRepository,
    private val reviews: ReviewRepository,
) {
    @GetMapping
    fun list(@PathVariable courseId: UUID): List<ReviewDto> =
        reviews.findByCourseIdOrderByCreatedAtDesc(courseId).map {
            ReviewDto(it.id!!, it.userId, it.rating, it.content,
                       it.createdAt.toString())
        }

    @PostMapping
    @Transactional
    fun upsert(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable courseId: UUID,
        @Valid @RequestBody req: ReviewRequest,
    ): ResponseEntity<ReviewDto> {
        val course = courses.findById(courseId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val existing = reviews.findByCourseIdAndUserId(courseId, userId)
        val saved = if (existing != null) {
            existing.rating = req.rating; existing.content = req.content
            reviews.save(existing)
        } else {
            reviews.save(Review(courseId = courseId, userId = userId,
                                  rating = req.rating, content = req.content))
        }
        // 평균 / 카운트 갱신
        val agg = reviews.aggregate(courseId)
        course.ratingAvg = (agg[0] as? Number)?.toDouble()
            ?.let { BigDecimal(it).setScale(2, RoundingMode.HALF_UP) }
        course.ratingCount = ((agg[1] as? Number)?.toInt() ?: 0)
        courses.save(course)
        return ResponseEntity.ok(ReviewDto(saved.id!!, saved.userId,
            saved.rating, saved.content, saved.createdAt.toString()))
    }

    @DeleteMapping
    @Transactional
    fun delete(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable courseId: UUID,
    ): ResponseEntity<Void> {
        val existing = reviews.findByCourseIdAndUserId(courseId, userId)
            ?: return ResponseEntity.notFound().build()
        reviews.deleteById(existing.id!!)
        val course = courses.findById(courseId).orElse(null)
        if (course != null) {
            val agg = reviews.aggregate(courseId)
            course.ratingAvg = (agg[0] as? Number)?.toDouble()
                ?.let { BigDecimal(it).setScale(2, RoundingMode.HALF_UP) }
            course.ratingCount = ((agg[1] as? Number)?.toInt() ?: 0)
            courses.save(course)
        }
        return ResponseEntity.noContent().build()
    }
}
