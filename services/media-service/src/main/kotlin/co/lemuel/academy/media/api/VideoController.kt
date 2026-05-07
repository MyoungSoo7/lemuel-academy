package co.lemuel.academy.media.api

import co.lemuel.academy.media.domain.Video
import co.lemuel.academy.media.domain.VideoRepository
import co.lemuel.academy.media.domain.VideoStatus
import co.lemuel.academy.media.r2.R2Service
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.time.OffsetDateTime
import java.util.UUID

@Configuration
class R2ServiceConfig {
    @Bean fun r2Service(presigner: S3Presigner,
                          @Value("\${academy.r2.bucket}") bucket: String) =
        R2Service(presigner, bucket)
}

data class UploadUrlRequest(
    @field:NotBlank val filename: String,
    @field:NotBlank val contentType: String,
)
data class UploadUrlResponse(val videoId: UUID, val uploadUrl: String, val key: String)

data class VideoDto(
    val id: UUID, val status: String,
    val hlsMasterUrl: String?, val thumbnailUrl: String?,
    val durationSec: Int?, val createdAt: OffsetDateTime,
)

@RestController
@RequestMapping("/api/media/videos")
class VideoController(
    private val videos: VideoRepository,
    private val r2: R2Service,
    private val redis: StringRedisTemplate,
    @Value("\${academy.ffmpeg.queue-stream}") private val queue: String,
) {
    @PostMapping("/upload-url")
    fun uploadUrl(
        @RequestHeader("X-User-Id") userId: UUID,
        @Valid @RequestBody req: UploadUrlRequest,
    ): UploadUrlResponse {
        val v = videos.save(Video(creatorId = userId,
                                   status = VideoStatus.UPLOADING))
        val key = r2.objectKey(v.id.toString(),
            "_" + req.filename.takeLast(64))
        val url = r2.presignPut(key, req.contentType)
        v.originalUrl = key
        videos.save(v)
        return UploadUrlResponse(v.id!!, url, key)
    }

    /** 클라이언트가 PUT 완료 후 호출 → transcode 큐 enqueue */
    @PostMapping("/{id}/finalize")
    fun finalize(@PathVariable id: UUID): ResponseEntity<VideoDto> {
        val v = videos.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        v.status = VideoStatus.UPLOADED
        videos.save(v)
        redis.opsForStream<String, String>().add(
            org.springframework.data.redis.connection.stream.StreamRecords
                .newRecord()
                .ofMap(mapOf("video_id" to id.toString()))
                .withStreamKey(queue)
        )
        return ResponseEntity.ok(v.toDto())
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<VideoDto> =
        videos.findById(id).map { ResponseEntity.ok(it.toDto()) }
            .orElse(ResponseEntity.notFound().build())

    private fun Video.toDto() = VideoDto(
        id!!, status.name, hlsMasterUrl, thumbnailUrl, durationSec, createdAt
    )
}
