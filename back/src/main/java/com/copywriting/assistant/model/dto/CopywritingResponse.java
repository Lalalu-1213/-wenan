package com.copywriting.assistant.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopywritingResponse {

    private String productName;
    private String style;
    private String generatedContent;
    private boolean fromCache;
    private LocalDateTime generatedAt;
}
