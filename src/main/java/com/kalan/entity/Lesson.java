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

    @Column(columnDefinition = "TEXT")
    private String notesBm;          // ← existait en DB mais pas dans l'entité !

    private String videoUrlEn;       // video_url_en
    private String videoUrlBm;       // video_url_bm

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private java.util.List<QuizQuestion> quizQuestions;

    public String getLocalizedNotes(String langCode) {
        return switch (langCode) {
            case "en" -> notesEn != null ? notesEn : notesFr;
            case "bm" -> notesBm != null ? notesBm : notesFr;
            default   -> notesFr;
        };
    }

    public String getLocalizedVideoUrl(String langCode) {
        return switch (langCode) {
            case "en" -> videoUrlEn != null ? videoUrlEn : videoUrl;
            case "bm" -> videoUrlBm != null ? videoUrlBm : videoUrl;
            default   -> videoUrl;
        };
    }

    public String getLocalizedTitle(String langCode) {
        return switch (langCode) {
            case "en" -> titleEn != null ? titleEn : titleFr;
            case "bm" -> titleBm != null ? titleBm : titleFr;
            default   -> titleFr;
        };
    }
    
}
