package com.copywriting.assistant.service.impl;

import com.copywriting.assistant.cache.CopywritingCache;
import com.copywriting.assistant.exception.BusinessException;
import com.copywriting.assistant.model.dto.*;
import com.copywriting.assistant.model.dto.openai.ChatMessage;
import com.copywriting.assistant.model.entity.BatchTask;
import com.copywriting.assistant.model.entity.CopywritingHistory;
import com.copywriting.assistant.model.enums.CopywritingStyle;
import com.copywriting.assistant.model.enums.TaskStatus;
import com.copywriting.assistant.repository.CopywritingHistoryRepository;
import com.copywriting.assistant.service.CopywritingService;
import com.copywriting.assistant.service.OpenAiService;
import com.copywriting.assistant.util.CacheKeyGenerator;
import com.copywriting.assistant.util.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CopywritingServiceImpl implements CopywritingService {

    private final OpenAiService openAiService;
    private final CopywritingCache copywritingCache;
    private final CopywritingHistoryRepository historyRepository;

    @Value("${batch.max-size:100}")
    private int batchMaxSize;

    // 内存存储批量任务状态
    private final Map<String, BatchTask> taskStore = new ConcurrentHashMap<>();

    @Override
    public CopywritingResponse generateCopywriting(CopywritingGenerateRequest request) {
        CopywritingStyle style = CopywritingStyle.fromCode(request.getStyle());
        ProductRequest product = request.getProduct();

        // 1. 查询缓存
        String cacheKey = CacheKeyGenerator.generateKey(product, style, request.getAdditionalRequirements());
        Optional<CopywritingResponse> cachedResponse = copywritingCache.get(cacheKey);
        if (cachedResponse.isPresent()) {
            log.info("命中缓存, productName: {}", product.getProductName());
            return cachedResponse.get();
        }

        // 2. 调用大模型API
        List<ChatMessage> messages = buildMessages(product, style, request.getAdditionalRequirements());
        String generatedContent = openAiService.chatCompletion(messages);

        // 3. 构建响应
        CopywritingResponse response = CopywritingResponse.builder()
                .productName(product.getProductName())
                .style(style.getName())
                .generatedContent(generatedContent)
                .fromCache(false)
                .generatedAt(LocalDateTime.now())
                .build();

        // 4. 写入缓存
        copywritingCache.put(cacheKey, response);

        // 5. 保存历史记录
        saveHistory(product, style, request.getAdditionalRequirements(), generatedContent, false, null);

        return response;
    }

    @Override
    public BatchTaskResponse submitBatchTask(BatchGenerateRequest request) {
        List<ProductRequest> products = request.getProducts();

        if (products.size() > batchMaxSize) {
            throw new BusinessException(400, "单次最多处理" + batchMaxSize + "条商品数据");
        }

        // 创建批量任务
        String taskId = UUID.randomUUID().toString();
        BatchTask task = BatchTask.builder()
                .taskId(taskId)
                .status(TaskStatus.PROCESSING)
                .totalCount(products.size())
                .completedCount(0)
                .failedCount(0)
                .results(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskStore.put(taskId, task);

        // 异步执行批量任务
        processBatchTask(taskId, products, request.getStyle(), request.getAdditionalRequirements());

        return BatchTaskResponse.builder()
                .taskId(taskId)
                .status(task.getStatus().getCode())
                .totalCount(task.getTotalCount())
                .completedCount(0)
                .failedCount(0)
                .message("批量任务已提交，共" + products.size() + "条数据")
                .build();
    }

    @Override
    public BatchTaskResponse getBatchTaskStatus(String taskId) {
        BatchTask task = taskStore.get(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }

        return BatchTaskResponse.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus().getCode())
                .totalCount(task.getTotalCount())
                .completedCount(task.getCompletedCount())
                .failedCount(task.getFailedCount())
                .results(task.getResults())
                .build();
    }

    @Async("asyncTaskExecutor")
    public void processBatchTask(String taskId, List<ProductRequest> products, String style, String additionalRequirements) {
        BatchTask task = taskStore.get(taskId);

        for (ProductRequest product : products) {
            try {
                CopywritingGenerateRequest generateRequest = CopywritingGenerateRequest.builder()
                        .product(product)
                        .style(style)
                        .additionalRequirements(additionalRequirements)
                        .build();

                CopywritingResponse response = generateCopywriting(generateRequest);
                task.addResult(response);
            } catch (Exception e) {
                log.error("处理商品失败: {}", product.getProductName(), e);
                task.incrementFailed();
            }
            task.setUpdatedAt(LocalDateTime.now());
        }

        task.setStatus(TaskStatus.COMPLETED);
        log.info("批量任务完成, taskId: {}, total: {}, success: {}, failed: {}",
                taskId, task.getTotalCount(), task.getCompletedCount(), task.getFailedCount());
    }

    private void saveHistory(ProductRequest product, CopywritingStyle style, String additionalRequirements,
                             String generatedContent, boolean fromCache, String taskId) {
        try {
            CopywritingHistory history = CopywritingHistory.builder()
                    .productName(product.getProductName())
                    .productDesc(product.getProductDesc())
                    .sellingPoints(product.getSellingPoints())
                    .targetAudience(product.getTargetAudience())
                    .style(style.getCode())
                    .generatedContent(generatedContent)
                    .additionalRequirements(additionalRequirements)
                    .fromCache(fromCache)
                    .taskId(taskId)
                    .build();
            historyRepository.save(history);
        } catch (Exception e) {
            log.warn("保存历史记录失败", e);
        }
    }

    private List<ChatMessage> buildMessages(ProductRequest product, CopywritingStyle style, String additionalRequirements) {
        List<ChatMessage> messages = new ArrayList<>();

        // 系统提示词
        messages.add(ChatMessage.builder()
                .role("system")
                .content(PromptTemplate.buildSystemPrompt(style))
                .build());

        // 用户提示词
        messages.add(ChatMessage.builder()
                .role("user")
                .content(PromptTemplate.buildUserPrompt(product, style, additionalRequirements))
                .build());

        return messages;
    }
}
