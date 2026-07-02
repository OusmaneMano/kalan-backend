package com.kalan.controller;

import com.kalan.dto.response.CourseResponse;
import com.kalan.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // GET /api/v1/courses?topic=java&level=beginner&language=fr&isFree=true&lang=fr&country=ML
    @GetMapping
    public ResponseEntity<List<CourseResponse>> list(
        @RequestParam(required = false) String topic,
        @RequestParam(required = false) String level,
        @RequestParam(required = false) String language,
        @RequestParam(required = false) Boolean isFree,
        @RequestParam(defaultValue = "fr") String lang,
        @RequestParam(defaultValue = "") String country
    ) {
        return ResponseEntity.ok(
            courseService.findAll(topic, level, language, isFree, lang, country));
    }

    // GET /api/v1/courses/{id}?lang=fr&country=ML
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> detail(
        @PathVariable Long id,
        @RequestParam(defaultValue = "fr") String lang,
        @RequestParam(defaultValue = "") String country
    ) {
        return ResponseEntity.ok(courseService.findById(id, lang, country));
    }

    // POST /api/v1/courses/{id}/enroll
    @PostMapping("/{id}/enroll")
    public ResponseEntity<Map<String, Object>> enroll(@PathVariable Long id) {
        var enrollment = courseService.enroll(id);
        return ResponseEntity.ok(Map.of(
            "enrolled", true,
            "courseId", id,
            "paymentStatus", enrollment.getPaymentStatus().name()
        ));
    }
}
