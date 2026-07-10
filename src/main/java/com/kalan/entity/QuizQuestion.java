package com.kalan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionFr;

    @Column(columnDefinition = "TEXT")
    private String questionEn;

    @Column(columnDefinition = "TEXT")
    private String questionBm;

    // Options stored as JSON array string e.g. ["int","String","boolean","double"]
    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionsFr;

    @Column(columnDefinition = "TEXT")
    private String optionsEn;

    @Column(columnDefinition = "TEXT")
    private String explanationFr;

    @Column(columnDefinition = "TEXT")
    private String explanationEn;

    @Column(columnDefinition = "TEXT")
    private String explanationBm;

    @Column(nullable = false)
    private Integer correctIndex;  // 0-based index of correct answer

    public String getLocalizedExplanation(String langCode) {
        return switch (langCode) {
            case "en" -> explanationEn != null ? explanationEn : explanationFr;
            case "bm" -> explanationBm != null ? explanationBm : explanationFr;
            default   -> explanationFr;
        };
    }

    public String getLocalizedQuestion(String langCode) {
        return switch (langCode) {
            case "en" -> questionEn != null ? questionEn : questionFr;
            case "bm" -> questionBm != null ? questionBm : questionFr;
            default   -> questionFr;
        };
    }

}