package com.copywriting.assistant.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200")
    private String productName;

    @Size(max = 1000, message = "商品描述长度不能超过1000")
    private String productDesc;

    @Size(max = 500, message = "商品卖点长度不能超过500")
    private String sellingPoints;

    @Size(max = 200, message = "目标人群长度不能超过200")
    private String targetAudience;
}
