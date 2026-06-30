package com.copywriting.assistant.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "copywriting_history")
public class CopywritingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_desc", length = 1000)
    private String productDesc;

    @Column(name = "selling_points", length = 500)
    private String sellingPoints;

    @Column(name = "target_audience", length = 200)
    private String targetAudience;

    @Column(name = "style", nullable = false, length = 50)
    private String style;

    @Column(name = "generated_content", columnDefinition = "TEXT")
    private String generatedContent;

    @Column(name = "additional_requirements", length = 500)
    private String additionalRequirements;

    @Column(name = "from_cache")
    private Boolean fromCache;

    @Column(name = "task_id", length = 50)
    private String taskId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
