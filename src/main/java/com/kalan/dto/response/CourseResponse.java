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
    Integer sortOrder,
    List<LessonResponse> lessons
) {
    public static CourseResponse from(Course course, String langCode, String countryCode) {
        return new CourseResponse(
            course.getId(),
            localizedTitle(course, langCode),
            localizedDesc(course, langCode),
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
            course.getSortOrder(),
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
            localizedTitle(course, langCode),
            localizedDesc(course, langCode),
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
            course.getSortOrder(),
            lessons
        );
    }

    private static String localizedTitle(Course course, String lang) {
        if ("en".equals(lang) && course.getTitleEn() != null && !course.getTitleEn().isBlank())
            return course.getTitleEn();
        if ("bm".equals(lang) && course.getTitleBm() != null && !course.getTitleBm().isBlank())
            return course.getTitleBm();
        return course.getTitleFr() != null ? course.getTitleFr() : "";
    }

    private static String localizedDesc(Course course, String lang) {
        if ("en".equals(lang) && course.getDescriptionEn() != null)
            return course.getDescriptionEn();
        return course.getDescriptionFr() != null ? course.getDescriptionFr() : "";
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
    String notesEn,
    String notesBm
) {
    static LessonResponse from(Lesson lesson, String langCode, boolean enrolled) {
        boolean locked = !lesson.isFree() && !enrolled;

        return new LessonResponse(
            lesson.getId(),
            lesson.getOrderIndex(),
            lesson.getLocalizedTitle(langCode),
            locked ? null : lesson.getLocalizedVideoUrl(langCode),
            lesson.getDurationSeconds(),
            lesson.isFree(),
            locked,
            locked ? null : lesson.getNotesFr(),
            locked ? null : lesson.getNotesEn(),
            locked ? null : lesson.getNotesBm()
        );
    }
}