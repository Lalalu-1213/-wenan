package com.copywriting.assistant.controller;

import com.copywriting.assistant.model.dto.*;
import com.copywriting.assistant.model.entity.CopywritingHistory;
import com.copywriting.assistant.repository.CopywritingHistoryRepository;
import com.copywriting.assistant.service.CopywritingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/copywriting")
@RequiredArgsConstructor
public class CopywritingController {

    private final CopywritingService copywritingService;
    private final CopywritingHistoryRepository historyRepository;

    /**
     * 生成单个商品文案
     */
    @PostMapping("/generate")
    public ApiResponse<CopywritingResponse> generate(@Valid @RequestBody CopywritingGenerateRequest request) {
        CopywritingResponse response = copywritingService.generateCopywriting(request);
        return ApiResponse.success(response);
    }

    /**
     * 提交批量生成任务
     */
    @PostMapping("/batch/submit")
    public ApiResponse<BatchTaskResponse> submitBatchTask(@Valid @RequestBody BatchGenerateRequest request) {
        BatchTaskResponse response = copywritingService.submitBatchTask(request);
        return ApiResponse.success("批量任务已提交", response);
    }

    /**
     * 查询批量任务状态
     */
    @GetMapping("/batch/{taskId}")
    public ApiResponse<BatchTaskResponse> getBatchTaskStatus(@PathVariable String taskId) {
        BatchTaskResponse response = copywritingService.getBatchTaskStatus(taskId);
        return ApiResponse.success(response);
    }

    /**
     * 查询历史记录
     */
    @GetMapping("/history")
    public ApiResponse<Page<CopywritingHistory>> getHistory(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CopywritingHistory> historyPage;
        if (keyword.isEmpty()) {
            historyPage = historyRepository.findAll(pageRequest);
        } else {
            historyPage = historyRepository.findByProductNameContaining(keyword, pageRequest);
        }
        return ApiResponse.success(historyPage);
    }

    /**
     * 获取最近生成记录
     */
    @GetMapping("/history/recent")
    public ApiResponse<List<CopywritingHistory>> getRecentHistory() {
        return ApiResponse.success(historyRepository.findTop10ByOrderByCreatedAtDesc());
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("ok");
    }
}
