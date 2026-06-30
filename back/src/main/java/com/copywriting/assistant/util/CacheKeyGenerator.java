package com.copywriting.assistant.util;

import com.copywriting.assistant.model.dto.ProductRequest;
import com.copywriting.assistant.model.enums.CopywritingStyle;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class CacheKeyGenerator {

    private static final String PREFIX = "copywriting:";

    private CacheKeyGenerator() {
    }

    /**
     * 生成缓存key
     * 基于商品信息和文案风格生成唯一key
     */
    public static String generateKey(ProductRequest product, CopywritingStyle style, String additionalRequirements) {
        String raw = String.join("|",
                product.getProductName(),
                product.getProductDesc() != null ? product.getProductDesc() : "",
                product.getSellingPoints() != null ? product.getSellingPoints() : "",
                product.getTargetAudience() != null ? product.getTargetAudience() : "",
                style.getCode(),
                additionalRequirements != null ? additionalRequirements : ""
        );

        return PREFIX + sha256(raw);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
