package com.copywriting.assistant.util;

import com.copywriting.assistant.model.dto.ProductRequest;
import com.copywriting.assistant.model.enums.CopywritingStyle;

public class PromptTemplate {

    private PromptTemplate() {
    }

    /**
     * 构建系统提示词
     */
    public static String buildSystemPrompt(CopywritingStyle style) {
        return switch (style) {
            case XIAOHONGSHU -> """
                    你是一位专业的小红书文案创作者，擅长撰写种草笔记。
                    要求：
                    1. 使用小红书特有的表达方式，语气亲切活泼
                    2. 适当使用emoji表情增加可读性
                    3. 突出产品使用体验和效果
                    4. 包含吸引人的标题
                    5. 内容长度控制在200-400字
                    6. 结尾引导互动（如点赞、收藏、评论）
                    """;

            case TAOBAO -> """
                    你是一位专业的淘宝商品文案撰写专家。
                    要求：
                    1. 突出产品核心卖点和差异化优势
                    2. 包含产品规格、材质等关键信息
                    3. 使用营销语言激发购买欲望
                    4. 适当提及优惠信息或限时活动
                    5. 内容结构清晰，便于快速浏览
                    6. 长度控制在300-500字
                    """;

            case WECHAT_MOMENTS -> """
                    你是一位朋友圈营销文案专家。
                    要求：
                    1. 语气生活化，像朋友推荐一样自然
                    2. 内容简短精炼，控制在100-200字
                    3. 引发好奇和互动欲望
                    4. 不要过于商业化，保持真实感
                    5. 可以适当使用悬念或提问句式
                    """;

            case TITLE_OPTIMIZE -> """
                    你是一位电商标题优化专家，擅长提升商品标题的搜索吸引力。
                    要求：
                    1. 提取商品核心关键词
                    2. 分析目标用户搜索习惯
                    3. 重组卖点，突出差异化
                    4. 控制标题长度在30字以内
                    5. 保证标题通顺自然
                    6. 提供3个优化后的标题供选择
                    """;
        };
    }

    /**
     * 构建用户提示词
     */
    public static String buildUserPrompt(ProductRequest product, CopywritingStyle style, String additionalRequirements) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("请为以下商品生成").append(style.getName()).append("的文案：\n\n");
        prompt.append("【商品名称】").append(product.getProductName()).append("\n");

        if (product.getProductDesc() != null && !product.getProductDesc().isEmpty()) {
            prompt.append("【商品描述】").append(product.getProductDesc()).append("\n");
        }

        if (product.getSellingPoints() != null && !product.getSellingPoints().isEmpty()) {
            prompt.append("【核心卖点】").append(product.getSellingPoints()).append("\n");
        }

        if (product.getTargetAudience() != null && !product.getTargetAudience().isEmpty()) {
            prompt.append("【目标人群】").append(product.getTargetAudience()).append("\n");
        }

        if (additionalRequirements != null && !additionalRequirements.isEmpty()) {
            prompt.append("\n【额外要求】").append(additionalRequirements).append("\n");
        }

        return prompt.toString();
    }
}
