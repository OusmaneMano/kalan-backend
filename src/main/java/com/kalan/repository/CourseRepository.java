package com.kalan.repository;

import com.kalan.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByPublishedTrue();

    @Query("""
        SELECT c FROM Course c WHERE c.published = true
        AND (:topic IS NULL OR c.topic = :topic)
        AND (:level IS NULL OR c.level = :level)
        AND (:language IS NULL OR c.language = :language)
        AND (:isFree IS NULL OR c.isFree = :isFree)
    """)
    List<Course> findWithFilters(
        @Param("topic") String topic,
        @Param("level") Course.Level level,
        @Param("language") String language,
        @Param("isFree") Boolean isFree
    );
}
