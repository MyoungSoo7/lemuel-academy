package co.lemuel.academy.catalog.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface CourseRepository : JpaRepository<Course, UUID> {
    fun findByStatusOrderByPublishedAtDesc(status: CourseStatus,
                                           pageable: Pageable): Page<Course>
    fun findByCreatorId(creatorId: UUID): List<Course>

    @Query(
        """SELECT c FROM Course c
           WHERE c.status = :status
             AND (:categoryId IS NULL OR c.categoryId = :categoryId)
             AND (:q IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')))
           ORDER BY c.publishedAt DESC NULLS LAST"""
    )
    fun search(
        status: CourseStatus,
        categoryId: Int?,
        q: String?,
        pageable: Pageable,
    ): Page<Course>
}

interface ChapterRepository : JpaRepository<Chapter, UUID> {
    fun findByCourseIdOrderByDisplayOrder(courseId: UUID): List<Chapter>
}

interface LessonRepository : JpaRepository<Lesson, UUID> {
    fun findByChapterIdOrderByDisplayOrder(chapterId: UUID): List<Lesson>
}
