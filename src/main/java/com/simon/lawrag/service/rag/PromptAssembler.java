package com.simon.lawrag.service.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * RAG 链路第5步：Prompt 组装
 * 将检索结果按法律逻辑顺序排列，注入角色设定、用户法律档案、对话历史等
 */
@Slf4j
@Component
public class PromptAssembler {

    /**
     * 组装完整 Prompt
     * @param query        用户问题（原始）
     * @param chunks       重排序后的 Top-K 切片
     * @param legalProfile 用户法律档案（JSON字符串）
     * @param history      对话历史（格式化文本）
     */
    public String assemble(String query, List<RetrievedChunk> chunks,
                            String legalProfile, String history) {
        // 构建参考法律文献文本
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunk chunk = chunks.get(i);
            contextBuilder.append("【参考文献").append(i + 1).append("】");

            if (chunk.getSourceName() != null) {
                contextBuilder.append("《").append(chunk.getSourceName()).append("》");
            }
            if (chunk.getChapter() != null && !chunk.getChapter().isEmpty()) {
                contextBuilder.append(" - ").append(chunk.getChapter());
            }
            if (chunk.getPageNumber() > 0) {
                contextBuilder.append(" 第").append(chunk.getPageNumber()).append("页");
            }
            contextBuilder.append("\n").append(chunk.getContent()).append("\n\n");
        }

        // 格式化法律档案
        String formattedLegal = formatLegalProfile(legalProfile);

        // 加载并填充 Prompt 模板
        String template = loadTemplate("legal_qa");
        String prompt = template
                .replace("{{question}}", query)
                .replace("{{context}}", contextBuilder.toString())
                .replace("{{legalProfile}}", formattedLegal)
                .replace("{{history}}", history != null ? history : "（无历史对话）");

        log.debug("Prompt组装完成: 参考文献={}条, 历史消息={}", chunks.size(),
                history != null ? "有" : "无");
        return prompt;
    }

    /**
     * 兜底 Prompt（检索置信度低时使用）
     * 保留对话历史，以支持"上一个问题是什么"等上下文引用类问题
     */
    public String assembleFallback(String query, String history) {
        String historySection = (history != null && !history.isBlank())
                ? "## 对话历史\n" + history + "\n\n"
                : "";
        return String.format("""
                你是一位专业的法律知识助手。

                %s## 用户当前问题
                %s

                当前知识库中未找到与该问题高度相关的法律文献。

                请先判断用户的问题类型：
                - 若用户在引用对话历史（如"上一个问题"、"刚才"、"之前"等），请直接根据上方【对话历史】作答，不要说"无法访问历史记录"。
                - 若是法律问题，请基于通用法律知识给出简要回答，并明确标注"⚠️ 知识库未匹配到相关文献，以下为通用法律知识"，建议咨询专业律师。
                """, historySection, query);
    }

    private String formatLegalProfile(String legalProfileJson) {
        if (legalProfileJson == null || legalProfileJson.isBlank()) {
            return "（用户未填写法律档案）";
        }
        try {
            com.alibaba.fastjson2.JSONObject profile =
                    com.alibaba.fastjson2.JSON.parseObject(legalProfileJson);
            StringBuilder sb = new StringBuilder();
            if (profile.containsKey("legalType") && !profile.getString("legalType").isBlank())
                sb.append("法律领域：").append(profile.get("legalType")).append("\n");
            if (profile.containsKey("region") && !profile.getString("region").isBlank())
                sb.append("所在地区：").append(profile.get("region")).append("\n");
            if (profile.containsKey("identity") && !profile.getString("identity").isBlank())
                sb.append("身份：").append(profile.get("identity")).append("\n");
            if (profile.containsKey("caseDesc") && !profile.getString("caseDesc").isBlank())
                sb.append("案情简述：").append(profile.get("caseDesc")).append("\n");
            return sb.isEmpty() ? "（用户未填写法律档案）" : sb.toString();
        } catch (Exception e) {
            return "（法律档案格式错误）";
        }
    }

    private String loadTemplate(String name) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/" + name + ".txt");
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Prompt模板加载失败: {}", name);
            return "{{question}}";
        }
    }
}
