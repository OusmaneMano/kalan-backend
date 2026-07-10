package com.kalan.controller;

import com.kalan.entity.QuizQuestion;
import com.kalan.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
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

        var result = questions.stream()
            .sorted(Comparator.comparing(QuizQuestion::getId))
            .map(q -> {
                String options = "en".equals(lang) && q.getOptionsEn() != null
                    ? q.getOptionsEn() : q.getOptionsFr();

                // HashMap (et pas Map.of) : accepte les valeurs null,
                // indispensable tant que explanation est null en base
                Map<String, Object> map = new HashMap<>();
                map.put("id",           q.getId());
                map.put("question",     q.getLocalizedQuestion(lang));
                map.put("options",      options);
                map.put("correctIndex", q.getCorrectIndex());
                map.put("explanation",  q.getLocalizedExplanation(lang));
                return map;
            })
            .toList();

        return ResponseEntity.ok(result);
    }
}