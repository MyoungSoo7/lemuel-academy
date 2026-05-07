package co.lemuel.academy.user.progress

import co.lemuel.academy.user.domain.Progress
import co.lemuel.academy.user.domain.ProgressId
import co.lemuel.academy.user.domain.ProgressRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.UUID

data class ReportProgressRequest(
    val lessonId: UUID,
    @field:Min(0) val watchedSeconds: Int,
    val completed: Boolean = false,
)

@RestController
@RequestMapping("/api/users/progress")
class ProgressController(
    private val repo: ProgressRepository,
) {
    @PostMapping
    fun report(
        @RequestHeader("X-User-Id") userId: UUID,
        @Valid @RequestBody req: ReportProgressRequest,
    ): Map<String, Any> {
        val id = ProgressId(userId, req.lessonId)
        val existing = repo.findById(id).orElse(null)
        val saved = if (existing != null) {
            existing.watchedSeconds =
                maxOf(existing.watchedSeconds, req.watchedSeconds)
            existing.completed = existing.completed || req.completed
            existing.lastWatchedAt = OffsetDateTime.now()
            repo.save(existing)
        } else {
            repo.save(Progress(userId = userId,
                                lessonId = req.lessonId,
                                watchedSeconds = req.watchedSeconds,
                                completed = req.completed))
        }
        return mapOf(
            "userId" to saved.userId,
            "lessonId" to saved.lessonId,
            "watchedSeconds" to saved.watchedSeconds,
            "completed" to saved.completed,
        )
    }

    @GetMapping
    fun list(@RequestHeader("X-User-Id") userId: UUID): List<Map<String, Any>> =
        repo.findByUserId(userId).map { p ->
            mapOf(
                "lessonId" to p.lessonId,
                "watchedSeconds" to p.watchedSeconds,
                "completed" to p.completed,
                "lastWatchedAt" to p.lastWatchedAt,
            )
        }
}
