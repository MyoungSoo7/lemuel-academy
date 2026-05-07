package co.lemuel.academy.catalog.api

import co.lemuel.academy.catalog.domain.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class CreateChapterRequest(
    @field:NotBlank val title: String,
    val displayOrder: Int = 0,
)
data class CreateLessonRequest(
    @field:NotBlank val title: String,
    val videoId: UUID? = null,
    val displayOrder: Int = 0,
    val isPreview: Boolean = false,
)

@RestController
class ChapterLessonController(
    private val courses: CourseRepository,
    private val chapters: ChapterRepository,
    private val lessons: LessonRepository,
) {
    @PostMapping("/api/catalog/courses/{courseId}/chapters")
    fun createChapter(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable courseId: UUID,
        @Valid @RequestBody req: CreateChapterRequest,
    ): ResponseEntity<Map<String, Any?>> {
        val c = courses.findById(courseId).orElse(null) ?: return ResponseEntity.notFound().build()
        if (c.creatorId != userId) return ResponseEntity.status(403).build()
        val saved = chapters.save(Chapter(courseId = courseId,
                                            title = req.title,
                                            displayOrder = req.displayOrder))
        return ResponseEntity.ok(mapOf(
            "id" to saved.id, "title" to saved.title,
            "displayOrder" to saved.displayOrder,
        ))
    }

    @PutMapping("/api/catalog/chapters/{id}")
    fun updateChapter(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody req: CreateChapterRequest,
    ): ResponseEntity<Map<String, Any?>> {
        val ch = chapters.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val course = courses.findById(ch.courseId).orElse(null) ?: return ResponseEntity.notFound().build()
        if (course.creatorId != userId) return ResponseEntity.status(403).build()
        ch.title = req.title; ch.displayOrder = req.displayOrder
        chapters.save(ch)
        return ResponseEntity.ok(mapOf("id" to ch.id))
    }

    @DeleteMapping("/api/catalog/chapters/{id}")
    fun deleteChapter(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        val ch = chapters.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val course = courses.findById(ch.courseId).orElse(null) ?: return ResponseEntity.notFound().build()
        if (course.creatorId != userId) return ResponseEntity.status(403).build()
        chapters.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/api/catalog/chapters/{chapterId}/lessons")
    fun createLesson(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable chapterId: UUID,
        @Valid @RequestBody req: CreateLessonRequest,
    ): ResponseEntity<Map<String, Any?>> {
        val ch = chapters.findById(chapterId).orElse(null) ?: return ResponseEntity.notFound().build()
        val course = courses.findById(ch.courseId).orElse(null) ?: return ResponseEntity.notFound().build()
        if (course.creatorId != userId) return ResponseEntity.status(403).build()
        val saved = lessons.save(Lesson(
            chapterId = chapterId, title = req.title,
            videoId = req.videoId, displayOrder = req.displayOrder,
            isPreview = req.isPreview,
        ))
        return ResponseEntity.ok(mapOf(
            "id" to saved.id, "title" to saved.title,
            "videoId" to saved.videoId,
        ))
    }

    @PutMapping("/api/catalog/lessons/{id}")
    fun updateLesson(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody req: CreateLessonRequest,
    ): ResponseEntity<Map<String, Any?>> {
        val l = lessons.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val ch = chapters.findById(l.chapterId).orElse(null) ?: return ResponseEntity.notFound().build()
        val course = courses.findById(ch.courseId).orElse(null) ?: return ResponseEntity.notFound().build()
        if (course.creatorId != userId) return ResponseEntity.status(403).build()
        l.title = req.title; l.videoId = req.videoId
        l.displayOrder = req.displayOrder; l.isPreview = req.isPreview
        lessons.save(l)
        return ResponseEntity.ok(mapOf("id" to l.id))
    }

    @DeleteMapping("/api/catalog/lessons/{id}")
    fun deleteLesson(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        val l = lessons.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val ch = chapters.findById(l.chapterId).orElse(null) ?: return ResponseEntity.notFound().build()
        val course = courses.findById(ch.courseId).orElse(null) ?: return ResponseEntity.notFound().build()
        if (course.creatorId != userId) return ResponseEntity.status(403).build()
        lessons.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
