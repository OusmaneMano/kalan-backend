package com.kalan.dto.response;

import com.kalan.entity.Course;
import com.kalan.entity.Lesson;
import java.util.List;

public record CourseResponse(
    Long id,
    String title,
    String description,
    String topic,
    String level,
    String language,
    String instructorName,
    String thumbnailUrl,
    boolean isFree,
    String price,
    Double rating,
    Integer studentCount,
    Integer lessonCount,
    List<LessonResponse> lessons
) {
    public static CourseResponse from(Course course, String langCode, String countryCode) {
        return new CourseResponse(
            course.getId(),
            course.getLocalizedTitle(langCode),
            langCode.equals("en") && course.getDescriptionEn() != null
                ? course.getDescriptionEn() : course.getDescriptionFr(),
            course.getTopic(),
            course.getLevel() != null ? course.getLevel().name().toLowerCase() : null,
            course.getLanguage(),
            course.getInstructorName(),
            course.getThumbnailUrl(),
            course.isFree(),
            course.getPriceForCountry(countryCode != null ? countryCode : ""),
            course.getRating(),
            course.getStudentCount(),
            course.getLessons() != null ? course.getLessons().size() : 0,
            null
        );
    }

    public static CourseResponse withLessons(Course course, String langCode,
                                             String countryCode, boolean enrolled) {
        List<LessonResponse> lessons = course.getLessons() != null
            ? course.getLessons().stream()
                .map(l -> LessonResponse.from(l, langCode, enrolled))
                .toList()
            : List.of();

        return new CourseResponse(
            course.getId(),
            course.getLocalizedTitle(langCode),
            langCode.equals("en") && course.getDescriptionEn() != null
                ? course.getDescriptionEn() : course.getDescriptionFr(),
            course.getTopic(),
            course.getLevel() != null ? course.getLevel().name().toLowerCase() : null,
            course.getLanguage(),
            course.getInstructorName(),
            course.getThumbnailUrl(),
            course.isFree(),
            course.getPriceForCountry(countryCode != null ? countryCode : ""),
            course.getRating(),
            course.getStudentCount(),
            lessons.size(),
            lessons
        );
    }
}

record LessonResponse(
    Long id,
    Integer orderIndex,
    String title,
    String videoUrl,
    Integer durationSeconds,
    boolean isFree,
    boolean isLocked,
    String notesFr,
    String notesEn
) {
    static LessonResponse from(Lesson lesson, String langCode, boolean enrolled) {
        boolean locked = !lesson.isFree() && !enrolled;
        return new LessonResponse(
            lesson.getId(),
            lesson.getOrderIndex(),
            lesson.getLocalizedTitle(langCode),
            locked ? null : lesson.getVideoUrl(),
            lesson.getDurationSeconds(),
            lesson.isFree(),
            locked,
            locked ? null : lesson.getNotesFr(),
            locked ? null : lesson.getNotesEn()
        );
    }
}