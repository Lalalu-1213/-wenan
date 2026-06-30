package com.copywriting.assistant.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchGenerateRequest {

    @NotEmpty(message = "商品列表不能为空")
    @Size(max = 100, message = "单次最多处理100条商品数据")
    @Valid
    private List<ProductRequest> products;

    @NotBlank(message = "文案风格不能为空")
    private String style;

    private String additionalRequirements;
}
