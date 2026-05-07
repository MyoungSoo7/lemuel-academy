package co.lemuel.academy.media.api

import co.lemuel.academy.media.domain.Video
import co.lemuel.academy.media.domain.VideoRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/media/videos")
class VideoListController(private val videos: VideoRepository) {

    /** 본인 업로드 영상 목록 (creator-studio 의 미리보기 / 강의 등록 시 선택용) */
    @GetMapping("/me")
    fun listMine(@RequestHeader("X-User-Id") userId: UUID): List<Map<String, Any?>> =
        videos.findByCreatorId(userId).map { it.toMap() }

    /** 영상 삭제 — 본인 또는 ADMIN 만 */
    @DeleteMapping("/{id}")
    fun delete(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestHeader("X-User-Role", required = false) role: String?,
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        val v = videos.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        if (v.creatorId != userId && role != "ADMIN")
            return ResponseEntity.status(403).build()
        videos.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    /** Manifest 직접 리턴 (또는 302 redirect — 클라이언트에서 hls.js 호환 위해 URL 노출) */
    @GetMapping("/{id}/manifest")
    fun manifest(@PathVariable id: UUID): ResponseEntity<Map<String, Any?>> =
        videos.findById(id).map { v ->
            ResponseEntity.ok(mapOf(
                "url" to v.hlsMasterUrl,
                "thumbnail" to v.thumbnailUrl,
                "duration_sec" to v.durationSec,
                "status" to v.status.name,
            ))
        }.orElse(ResponseEntity.notFound().build())

    private fun Video.toMap() = mapOf(
        "id" to id, "status" to status.name,
        "hlsMasterUrl" to hlsMasterUrl, "thumbnailUrl" to thumbnailUrl,
        "durationSec" to durationSec, "createdAt" to createdAt,
        "errorMessage" to errorMessage,
    )
}
