package com.kalan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Multilingual titles — stored as JSON or separate columns
    @Column(nullable = false)
    private String titleFr;

    private String titleEn;
    private String titleBm;  // Bambara
    private String titleWo;  // Wolof

    // Multilingual descriptions
    @Column(columnDefinition = "TEXT")
    private String descriptionFr;

    @Column(columnDefinition = "TEXT")
    private String descriptionEn;

    private String topic;    // java, flutter, sql, web, devops

    @Enumerated(EnumType.STRING)
    private Level level;

    private String language; // fr, bm, en, wo, pt

    private String instructorName;

    private String thumbnailUrl;

    private boolean isFree;

    // Pricing by region
    private Double priceXof;  // FCFA — West Africa
    private Double priceEur;  // Europe diaspora
    private Double priceUsd;  // USA diaspora

    private Double rating = 0.0;
    private Integer studentCount = 0;

    // Fullstack path order: java=1, sql=2, git=3, spring boot=4, flutter=5.
    // Lower shows first. Null sorts last.
    private Integer sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean published = false;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @OrderBy("orderIndex ASC")
    private List<Lesson> lessons;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;

    public enum Level {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    // Returns localized title based on language code
    public String getLocalizedTitle(String langCode) {
        return switch (langCode) {
            case "en" -> titleEn != null ? titleEn : titleFr;
            case "bm" -> titleBm != null ? titleBm : titleFr;
            case "wo" -> titleWo != null ? titleWo : titleFr;
            default   -> titleFr;
        };
    }

    // Returns price for user's region
    public String getPriceForCountry(String countryCode) {
        if (isFree) return "0";
        return switch (countryCode) {
            case "ML", "SN", "CI", "BF", "GN", "TG", "BJ", "NE" ->
                priceXof != null ? priceXof.intValue() + " FCFA" : "5000 FCFA";
            case "FR", "DE", "BE", "CH", "LU" ->
                priceEur != null ? priceEur + " €" : "8 €";
            default ->
                priceUsd != null ? "$" + priceUsd : "$9";
        };
    }
}