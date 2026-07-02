package com.kalan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Integer orderIndex;

    // Multilingual titles
    @Column(nullable = false)
    private String titleFr;
    private String titleEn;
    private String titleBm;

    // Multilingual notes (markdown)
    @Column(columnDefinition = "TEXT")
    private String notesFr;

    @Column(columnDefinition = "TEXT")
    private String notesEn;

    private String videoUrl;      // Cloudflare R2 URL

    private Integer durationSeconds;

    private boolean isFree;       // first 3 lessons free

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private java.util.List<QuizQuestion> quizQuestions;

    public String getLocalizedTitle(String langCode) {
        return switch (langCode) {
            case "en" -> titleEn != null ? titleEn : titleFr;
            case "bm" -> titleBm != null ? titleBm : titleFr;
            default   -> titleFr;
        };
    }
}
