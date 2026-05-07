package co.lemuel.academy.catalog.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

enum class CourseStatus { DRAFT, REVIEW, PUBLISHED, REJECTED }

@Entity
@Table(name = "courses", schema = "catalog")
class Course(
    @Id @GeneratedValue var id: UUID? = null,
    @Column(name = "creator_id", nullable = false) var creatorId: UUID,
    @Column(nullable = false) var title: String,
    @Column(columnDefinition = "TEXT") var description: String? = null,
    @Column(name = "thumbnail_url") var thumbnailUrl: String? = null,
    @Column(name = "category_id") var categoryId: Int? = null,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: CourseStatus = CourseStatus.DRAFT,
    @Column(name = "view_count") var viewCount: Long = 0,
    @Column(name = "rating_avg", precision = 3, scale = 2) var ratingAvg: BigDecimal? = null,
    @Column(name = "rating_count") var ratingCount: Int = 0,
    @Column(name = "created_at") var createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "published_at") var publishedAt: OffsetDateTime? = null,
)

@Entity
@Table(name = "chapters", schema = "catalog")
class Chapter(
    @Id @GeneratedValue var id: UUID? = null,
    @Column(name = "course_id", nullable = false) var courseId: UUID,
    @Column(nullable = false) var title: String,
    @Column(name = "display_order", nullable = false) var displayOrder: Int = 0,
)

@Entity
@Table(name = "lessons", schema = "catalog")
class Lesson(
    @Id @GeneratedValue var id: UUID? = null,
    @Column(name = "chapter_id", nullable = false) var chapterId: UUID,
    @Column(nullable = false) var title: String,
    @Column(name = "video_id") var videoId: UUID? = null,
    @Column(name = "duration_sec") var durationSec: Int? = null,
    @Column(name = "display_order", nullable = false) var displayOrder: Int = 0,
    @Column(name = "is_preview") var isPreview: Boolean = false,
)
