package com.copywriting.assistant.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CopywritingStyle {

    XIAOHONGSHU("xiaohongshu", "小红书风格", "种草风格，使用emoji，语气亲切活泼，强调使用体验和效果"),
    TAOBAO("taobao", "淘宝风格", "电商详情风格，突出产品卖点、规格参数、优惠信息"),
    WECHAT_MOMENTS("wechat", "朋友圈风格", "生活化语气，简短精炼，引发好奇和互动"),
    TITLE_OPTIMIZE("title", "标题优化", "提取关键词，重组卖点，提升搜索吸引力");

    private final String code;
    private final String name;
    private final String description;

    public static CopywritingStyle fromCode(String code) {
        for (CopywritingStyle style : values()) {
            if (style.code.equalsIgnoreCase(code)) {
                return style;
            }
        }
        throw new IllegalArgumentException("不支持的文案风格: " + code);
    }
}
