package com.kalan.controller;

import com.kalan.entity.QuizQuestion;
import com.kalan.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class QuizController {

    private final LessonRepository lessonRepository;

    @GetMapping("/{lessonId}/quiz")
    public ResponseEntity<List<Map<String, Object>>> getQuiz(
        @PathVariable Long lessonId,
        @RequestParam(defaultValue = "fr") String lang
    ) {
        var lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));

        var questions = lesson.getQuizQuestions();
        if (questions == null || questions.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        var result = questions.stream().map(q -> {
            String question = lang.equals("en") && q.getQuestionEn() != null
                ? q.getQuestionEn() : q.getQuestionFr();
            String options = lang.equals("en") && q.getOptionsEn() != null
                ? q.getOptionsEn() : q.getOptionsFr();

            return Map.<String, Object>of(
                "id",           q.getId(),
                "question",     question,
                "options",      options,
                "correctIndex", q.getCorrectIndex()
            );
        }).toList();

        return ResponseEntity.ok(result);
    }
}