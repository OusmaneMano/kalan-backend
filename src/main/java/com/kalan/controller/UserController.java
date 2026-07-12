package com.kalan.controller;

import com.kalan.entity.*;
import com.kalan.repository.*;
import com.kalan.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;

    // GET /api/v1/user/profile
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile() {
        User user = getCurrentUser();
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
        long completed = enrollments.stream().filter(Enrollment::isCompleted).count();

        long lessonsCompleted = lessonProgressRepository.countCompletedByUser(user.getId());
        long watchedSeconds   = lessonProgressRepository.sumWatchedSecondsByUser(user.getId());
        long hoursLearned     = watchedSeconds / 3600;

        Map<String, Object> body = new HashMap<>();
        body.put("id",                user.getId());
        body.put("email",             user.getEmail());
        body.put("fullName",          user.getFullName());
        body.put("country",           user.getCountry() != null ? user.getCountry() : "");
        body.put("preferredLanguage", user.getPreferredLanguage());
        body.put("role",              user.getRole().name());
        body.put("createdAt",         user.getCreatedAt().toString());
        body.put("coursesEnrolled",   enrollments.size());
        body.put("coursesCompleted",  completed);
        body.put("lessonsCompleted",  lessonsCompleted);
        body.put("hoursLearned",      hoursLearned);
        return ResponseEntity.ok(body);
    }

    // GET /api/v1/user/progress
    @GetMapping("/progress")
    public ResponseEntity<List<Map<String, Object>>> progress() {
        User user = getCurrentUser();
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());

        List<Map<String, Object>> result = enrollments.stream().map(e -> {
            long completedLessons = lessonProgressRepository
                .countCompletedByUserAndCourse(user.getId(), e.getCourse().getId());
            int totalLessons = e.getCourse().getLessons() != null
                ? e.getCourse().getLessons().size() : 0;

            Map<String, Object> item = new HashMap<>();
            item.put("courseId",         e.getCourse().getId());
            item.put("courseTitle",      e.getCourse().getTitleFr());
            item.put("completedLessons", completedLessons);
            item.put("totalLessons",     totalLessons);
            item.put("isCompleted",      e.isCompleted());
            item.put("enrolledAt",       e.getEnrolledAt().toString());
            item.put("completedAt",      e.getCompletedAt() != null ? e.getCompletedAt().toString() : null);
            return item;
        }).toList();

        return ResponseEntity.ok(result);
    }

    // POST /api/v1/user/progress/lesson/{lessonId}
    @PostMapping("/progress/lesson/{lessonId}")
    public ResponseEntity<Map<String, Object>> updateLessonProgress(
        @PathVariable Long lessonId,
        @RequestBody Map<String, Object> body
    ) {
        User user = getCurrentUser();
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));

        LessonProgress progress = lessonProgressRepository
            .findByUserIdAndLessonId(user.getId(), lessonId)
            .orElse(LessonProgress.builder().user(user).lesson(lesson).build());

        boolean completed = Boolean.TRUE.equals(body.get("completed"));
        Integer watchedSeconds = body.get("watchedSeconds") instanceof Integer
            ? (Integer) body.get("watchedSeconds") : 0;
        Integer quizScore = body.get("quizScore") instanceof Integer
            ? (Integer) body.get("quizScore") : null;

        progress.setWatchedSeconds(watchedSeconds);
        if (completed && !progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }
        if (quizScore != null) progress.setQuizScore(quizScore);

        lessonProgressRepository.save(progress);

        // Check if entire course is now complete
        checkCourseCompletion(user, lesson.getCourse());

        return ResponseEntity.ok(Map.of(
            "lessonId",      lessonId,
            "completed",     progress.isCompleted(),
            "watchedSeconds",progress.getWatchedSeconds()
        ));
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private void checkCourseCompletion(User user, Course course) {
        if (course.getLessons() == null) return;
        long total = course.getLessons().size();
        long done  = lessonProgressRepository
            .countCompletedByUserAndCourse(user.getId(), course.getId());

        if (done >= total) {
            enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId())
                .ifPresent(e -> {
                    if (!e.isCompleted()) {
                        e.setCompletedAt(LocalDateTime.now());
                        enrollmentRepository.save(e);
                    }
                });
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}