package co.lemuel.academy.catalog.api

import co.lemuel.academy.catalog.domain.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.UUID

data class CourseDto(
    val id: UUID, val creatorId: UUID, val title: String,
    val description: String?, val thumbnailUrl: String?,
    val categoryId: Int?, val status: String,
    val ratingAvg: Double?, val ratingCount: Int,
    val createdAt: OffsetDateTime, val publishedAt: OffsetDateTime?,
)

fun Course.toDto() = CourseDto(
    id!!, creatorId, title, description, thumbnailUrl, categoryId,
    status.name, ratingAvg?.toDouble(), ratingCount, createdAt, publishedAt,
)

data class CreateCourseRequest(
    @field:NotBlank val title: String,
    val description: String? = null,
    val categoryId: Int? = null,
)

data class CourseDetailDto(
    val course: CourseDto,
    val chapters: List<ChapterWithLessonsDto>,
)
data class ChapterWithLessonsDto(val id: UUID, val title: String,
                                  val displayOrder: Int,
                                  val lessons: List<LessonDto>)
data class LessonDto(val id: UUID, val title: String,
                      val videoId: UUID?, val durationSec: Int?,
                      val displayOrder: Int, val isPreview: Boolean)

@RestController
@RequestMapping("/api/catalog/courses")
class CourseController(
    private val courses: CourseRepository,
    private val chapters: ChapterRepository,
    private val lessons: LessonRepository,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) categoryId: Int?,
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): Map<String, Any> {
        val res: Page<Course> = courses.search(
            CourseStatus.PUBLISHED, categoryId, q,
            PageRequest.of(page, size.coerceAtMost(100)),
        )
        return mapOf(
            "items" to res.content.map { it.toDto() },
            "page" to res.number,
            "size" to res.size,
            "total" to res.totalElements,
        )
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: UUID): ResponseEntity<CourseDetailDto> {
        val c = courses.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val chs = chapters.findByCourseIdOrderByDisplayOrder(c.id!!)
        val withLessons = chs.map { ch ->
            val ls = lessons.findByChapterIdOrderByDisplayOrder(ch.id!!)
            ChapterWithLessonsDto(
                ch.id!!, ch.title, ch.displayOrder,
                ls.map { LessonDto(it.id!!, it.title, it.videoId,
                                    it.durationSec, it.displayOrder, it.isPreview) }
            )
        }
        // viewCount 증가
        c.viewCount += 1
        courses.save(c)
        return ResponseEntity.ok(CourseDetailDto(c.toDto(), withLessons))
    }

    @PostMapping
    fun create(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestHeader("X-User-Role") role: String,
        @Valid @RequestBody req: CreateCourseRequest,
    ): ResponseEntity<CourseDto> {
        if (role != "CREATOR" && role != "ADMIN")
            return ResponseEntity.status(403).build()
        val saved = courses.save(Course(
            creatorId = userId,
            title = req.title,
            description = req.description,
            categoryId = req.categoryId,
            status = CourseStatus.DRAFT,
        ))
        return ResponseEntity.ok(saved.toDto())
    }

    @PostMapping("/{id}/submit-review")
    fun submitReview(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID,
    ): ResponseEntity<CourseDto> {
        val c = courses.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        if (c.creatorId != userId) return ResponseEntity.status(403).build()
        if (c.status != CourseStatus.DRAFT && c.status != CourseStatus.REJECTED)
            return ResponseEntity.status(409).build()
        c.status = CourseStatus.REVIEW
        return ResponseEntity.ok(courses.save(c).toDto())
    }

    @PatchMapping("/{id}/review")
    fun adminReview(
        @RequestHeader("X-User-Role") role: String,
        @PathVariable id: UUID,
        @RequestParam approved: Boolean,
    ): ResponseEntity<CourseDto> {
        if (role != "ADMIN") return ResponseEntity.status(403).build()
        val c = courses.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        if (c.status != CourseStatus.REVIEW) return ResponseEntity.status(409).build()
        c.status = if (approved) CourseStatus.PUBLISHED else CourseStatus.REJECTED
        if (approved) c.publishedAt = OffsetDateTime.now()
        return ResponseEntity.ok(courses.save(c).toDto())
    }
}
