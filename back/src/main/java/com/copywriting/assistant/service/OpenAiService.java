package com.copywriting.assistant.service;

import com.copywriting.assistant.config.OpenAiConfig;
import com.copywriting.assistant.exception.BusinessException;
import com.copywriting.assistant.model.dto.openai.ChatCompletionRequest;
import com.copywriting.assistant.model.dto.openai.ChatCompletionResponse;
import com.copywriting.assistant.model.dto.openai.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final OpenAiConfig openAiConfig;
    private final ObjectMapper objectMapper;

    /**
     * 调用大模型API生成文案
     */
    public String chatCompletion(List<ChatMessage> messages) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(openAiConfig.getModel())
                .messages(messages)
                .maxTokens(openAiConfig.getMaxTokens())
                .temperature(openAiConfig.getTemperature())
                .build();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = openAiConfig.getBaseUrl() + "/v1/chat/completions";

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + openAiConfig.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(request), ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes());

                if (statusCode != 200) {
                    log.error("OpenAI API调用失败, statusCode: {}, response: {}", statusCode, responseBody);
                    throw new BusinessException(500, "大模型API调用失败: " + responseBody);
                }

                ChatCompletionResponse chatResponse = objectMapper.readValue(responseBody, ChatCompletionResponse.class);

                if (chatResponse.getChoices() == null || chatResponse.getChoices().isEmpty()) {
                    throw new BusinessException(500, "大模型返回结果为空");
                }

                return chatResponse.getChoices().get(0).getMessage().getContent();
            });
        } catch (IOException e) {
            log.error("调用OpenAI API异常", e);
            throw new BusinessException(500, "调用大模型API异常: " + e.getMessage());
        }
    }
}
