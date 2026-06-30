package com.copywriting.assistant.model.entity;

import com.copywriting.assistant.model.dto.CopywritingResponse;
import com.copywriting.assistant.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTask {

    private String taskId;
    private TaskStatus status;
    private int totalCount;
    private int completedCount;
    private int failedCount;
    private List<CopywritingResponse> results;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void addResult(CopywritingResponse response) {
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(response);
        completedCount++;
    }

    public void incrementFailed() {
        failedCount++;
    }

    public boolean isFinished() {
        return (completedCount + failedCount) >= totalCount;
    }
}
