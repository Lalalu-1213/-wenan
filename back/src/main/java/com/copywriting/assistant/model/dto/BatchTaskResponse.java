package com.copywriting.assistant.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTaskResponse {

    private String taskId;
    private String status;
    private int totalCount;
    private int completedCount;
    private int failedCount;
    private List<CopywritingResponse> results;
    private String message;
}
