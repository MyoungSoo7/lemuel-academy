package co.lemuel.academy.user.domain

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

enum class Role { STUDENT, CREATOR, ADMIN }

@Entity
@Table(name = "users", schema = "users_svc")
class User(
    @Id
    @GeneratedValue
    var id: UUID? = null,

    @Column(unique = true, nullable = false)
    var email: String,

    @Column(name = "password_hash")
    var passwordHash: String? = null,

    @Column(name = "oauth_provider")
    var oauthProvider: String? = null,

    @Column(name = "oauth_id")
    var oauthId: String? = null,

    @Column(name = "display_name", nullable = false)
    var displayName: String,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.STUDENT,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)

@Entity
@Table(name = "progress", schema = "users_svc")
@IdClass(ProgressId::class)
class Progress(
    @Id @Column(name = "user_id") var userId: UUID,
    @Id @Column(name = "lesson_id") var lessonId: UUID,
    @Column(name = "watched_seconds", nullable = false) var watchedSeconds: Int = 0,
    @Column(nullable = false) var completed: Boolean = false,
    @Column(name = "last_watched_at") var lastWatchedAt: OffsetDateTime = OffsetDateTime.now()
)

data class ProgressId(val userId: UUID = UUID.randomUUID(),
                      val lessonId: UUID = UUID.randomUUID()) : java.io.Serializable
