package com.simon.lawrag.service.rag;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * RAG 链路第1步：Query 改写
 * 将用户口语化问题改写为专业检索词（招生场景）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryRewriter {

    private final OpenAiChatModel chatModel;

    private static final int MAX_QUERY_LENGTH = 200;

    /**
     * 改写用户查询
     * @param originalQuery 原始用户输入
     * @return 改写后的专业查询词
     */
    public String rewrite(String originalQuery) {
        // 过短的查询不需要改写
        if (originalQuery.length() <= 10) {
            log.debug("查询过短，跳过改写: {}", originalQuery);
            return originalQuery;
        }

        try {
            String prompt = loadPromptTemplate("query_rewrite")
                    .replace("{{question}}", originalQuery);

            String rewritten = chatModel.generate(UserMessage.from(prompt))
                    .content()
                    .text()
                    .trim();

            // 截断过长结果
            if (rewritten.length() > MAX_QUERY_LENGTH) {
                rewritten = rewritten.substring(0, MAX_QUERY_LENGTH);
            }

            log.info("Query改写: [{}] → [{}]", originalQuery, rewritten);
            return rewritten;

        } catch (Exception e) {
            log.warn("Query改写失败，使用原始查询: {}", e.getMessage());
            return originalQuery;
        }
    }

    private String loadPromptTemplate(String name) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/" + name + ".txt");
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Prompt模板加载失败: {}", name);
            return "{{question}}";
        }
    }
}
