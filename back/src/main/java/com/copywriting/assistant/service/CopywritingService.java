package com.copywriting.assistant.service;

import com.copywriting.assistant.model.dto.BatchGenerateRequest;
import com.copywriting.assistant.model.dto.BatchTaskResponse;
import com.copywriting.assistant.model.dto.CopywritingGenerateRequest;
import com.copywriting.assistant.model.dto.CopywritingResponse;

public interface CopywritingService {

    /**
     * 生成单个商品文案
     */
    CopywritingResponse generateCopywriting(CopywritingGenerateRequest request);

    /**
     * 提交批量生成任务
     */
    BatchTaskResponse submitBatchTask(BatchGenerateRequest request);

    /**
     * 查询批量任务状态
     */
    BatchTaskResponse getBatchTaskStatus(String taskId);
}
