package co.lemuel.academy.media.domain

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import java.util.UUID

enum class VideoStatus { UPLOADING, UPLOADED, TRANSCODING, READY, FAILED }

@Entity
@Table(name = "videos", schema = "media")
class Video(
    @Id @GeneratedValue var id: UUID? = null,
    @Column(name = "creator_id", nullable = false) var creatorId: UUID,
    @Column(name = "original_url") var originalUrl: String? = null,
    @Column(name = "hls_master_url") var hlsMasterUrl: String? = null,
    @Column(name = "thumbnail_url") var thumbnailUrl: String? = null,
    @Column(name = "duration_sec") var durationSec: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: VideoStatus = VideoStatus.UPLOADING,
    @Column(name = "error_message") var errorMessage: String? = null,
    @Column(name = "created_at") var createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "ready_at") var readyAt: OffsetDateTime? = null,
)

interface VideoRepository : JpaRepository<Video, UUID> {
    fun findByCreatorId(creatorId: UUID): List<Video>
    fun findByStatus(status: VideoStatus): List<Video>
}
