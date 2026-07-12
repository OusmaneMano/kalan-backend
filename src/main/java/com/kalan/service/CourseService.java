package com.kalan.service;

import com.kalan.dto.response.CourseResponse;
import com.kalan.entity.Course;
import com.kalan.entity.Enrollment;
import com.kalan.entity.User;
import com.kalan.repository.CourseRepository;
import com.kalan.repository.EnrollmentRepository;
import com.kalan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    // When false (default), any enrollment unlocks content (dev/testing).
    // Flip to true once a payment provider is wired: then paid courses need
    // paymentStatus FREE or PAID to unlock.
    @Value("${kalan.payments.enforce:false}")
    private boolean enforcePayments;

    // ── List all published courses with optional filters ──────────────────────
    public List<CourseResponse> findAll(String topic, String level, String language,
                                        Boolean isFree, String langCode, String country) {
        Course.Level levelEnum = level != null ? Course.Level.valueOf(level.toUpperCase()) : null;
        List<Course> courses = courseRepository.findWithFilters(topic, levelEnum, language, isFree);
        return courses.stream()
            .map(c -> CourseResponse.from(c, langCode, country))
            .toList();
    }

    // ── Course detail with lesson list ────────────────────────────────────────
    public CourseResponse findById(Long id, String langCode, String country) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        boolean enrolled = hasAccess(id);
        return CourseResponse.withLessons(course, langCode, country, enrolled);
    }

    // ── Does the current user have unlockable access to a course? ─────────────
    // Free courses: always. Paid: enrollment must be FREE/PAID when enforcing;
    // when not enforcing, any enrollment counts (so dev/testing still works).
    public boolean hasAccess(Long courseId) {
        try {
            User user = getCurrentUser();
            var enr = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId);
            if (enr.isEmpty()) return false;
            if (!enforcePayments) return true;
            var status = enr.get().getPaymentStatus();
            return status == Enrollment.PaymentStatus.FREE
                || status == Enrollment.PaymentStatus.PAID;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Enroll current user in a course ──────────────────────────────────────
    public Enrollment enroll(Long courseId) {
        User user = getCurrentUser();
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            return enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId).get();
        }

        Enrollment enrollment = Enrollment.builder()
            .user(user)
            .course(course)
            .paymentStatus(course.isFree()
                ? Enrollment.PaymentStatus.FREE
                : Enrollment.PaymentStatus.PENDING)
            .build();

        // Update student count
        course.setStudentCount(course.getStudentCount() + 1);
        courseRepository.save(course);

        return enrollmentRepository.save(enrollment);
    }

    // ── Check if current user is enrolled ────────────────────────────────────
    public boolean isEnrolled(Long courseId) {
        try {
            User user = getCurrentUser();
            return enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId);
        } catch (Exception e) {
            return false;
        }
    }

    // ── Get current user from security context ────────────────────────────────
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}