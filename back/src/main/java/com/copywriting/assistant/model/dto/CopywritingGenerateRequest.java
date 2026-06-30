package com.copywriting.assistant.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopywritingGenerateRequest {

    @NotNull(message = "商品信息不能为空")
    @Valid
    private ProductRequest product;

    @NotBlank(message = "文案风格不能为空")
    private String style;

    private String additionalRequirements;
}
