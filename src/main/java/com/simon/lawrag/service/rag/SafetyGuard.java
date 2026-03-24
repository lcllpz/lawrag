package com.simon.lawrag.service.rag;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.simon.lawrag.config.AiConfigHolder;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * RAG 链路第6步（安全兜底）：招生信息安全检查
 * 1. 检测紧急人身安全情况（立即触发报警提示）
 * 2. 评估检索置信度（低置信度触发兜底机制）
 * 3. 答案自评（回答是否基于检索内容）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SafetyGuard {

    private final OpenAiChatModel chatModel;
    private final AiConfigHolder aiConfigHolder;

    // 紧急人身安全关键词（需要立即报警）
    private static final Set<String> EMERGENCY_KEYWORDS = Set.of(
            "家庭暴力", "家暴", "非法拘禁", "绑架", "人身伤害", "性侵", "强奸",
            "威胁生命", "恐吓", "跟踪骚扰", "强制猥亵", "故意伤害", "人身安全",
            "被追杀", "劫持", "抢劫", "持刀威胁", "生命危险"
    );

    /**
     * 检查是否包含紧急人身安全情况
     */
    public boolean isEmergency(String query) {
        String lowerQuery = query.toLowerCase();
        for (String keyword : EMERGENCY_KEYWORDS) {
            if (lowerQuery.contains(keyword)) {
                log.warn("检测到紧急人身安全关键词: {}", keyword);
                return true;
            }
        }
        return false;
    }

    /**
     * 评估检索置信度（基于得分分布）
     * @param topChunks Top-K 重排序后的切片
     * @return 置信度 0.0-1.0
     */
    public float evaluateConfidence(java.util.List<RetrievedChunk> topChunks) {
        if (topChunks == null || topChunks.isEmpty()) return 0f;

        // 取最高分
        float maxScore = topChunks.stream()
                .map(c -> c.getRerankScore() > 0 ? c.getRerankScore() : c.getScore())
                .max(Float::compareTo)
                .orElse(0f);

        // 简单线性映射
        float confidence = Math.min(1.0f, Math.max(0.0f, maxScore));
        log.debug("检索置信度: {:.3f}", confidence);
        return confidence;
    }

    /**
     * 判断是否需要触发兜底
     */
    public boolean needsFallback(java.util.List<RetrievedChunk> topChunks) {
        float threshold = aiConfigHolder.getFloat("safety.confidence_threshold");
        return evaluateConfidence(topChunks) < threshold;
    }

    /**
     * 生成紧急人身安全提示（追加到回答末尾）
     */
    public String getEmergencyWarning() {
        return "\n\n---\n" +
               "⚠️ **紧急提示**：您描述的情况可能涉及人身安全！\n" +
               "**请立即拨打报警电话 110** 或前往最近的派出所寻求帮助。\n" +
               "如有人身伤害，同时拨打急救电话 120。不要延误求助时间。";
    }

    /**
     * 生成兜底提示（追加到回答末尾）
     */
    public String getFallbackNotice() {
        return "\n\n---\n" +
               "ℹ️ **说明**：当前知识库中未找到与您问题高度相关的招生资料，" +
               "以上回答基于通用招生信息，仅供参考。\n" +
               "**建议联系学校招生办核实**，并以学校官网与省级考试院最新公告为准。";
    }

    /**
     * 无检索支撑时的友好提示（不再让模型自由发挥）
     */
    public String getNoDataFriendlyNotice() {
        return """
                暂无可支撑该问题的有效招生数据。

                可能原因：
                - 当前知识库未收录该学校/该年份/该省份的对应数据
                - 已收录文档为规则性文件，未包含具体录取分数或位次统计

                建议您补充以下信息后重试：
                - 学校名称、招生年份、省份、批次、专业（或专业组）

                也可先在管理端上传以下资料：
                - 当年招生计划（分省分专业）
                - 历年录取分数线/位次数据
                - 学校官方招生章程与公告
                """;
    }

    /**
     * LLM 答案自评（二次评估）
     */
    public SafetyCheckResult evaluateAnswer(String answer) {
        try {
            String template = loadTemplate("safety_check");
            String prompt = template.replace("{{answer}}", answer);

            String response = chatModel.generate(UserMessage.from(prompt))
                    .content().text().trim();

            // 清理可能的 markdown 代码块
            response = response.replaceAll("```json|```", "").trim();
            JSONObject result = JSON.parseObject(response);

            return new SafetyCheckResult(
                    result.getBooleanValue("hasSafetyRisk"),
                    result.getBooleanValue("isEmergency"),
                    result.getFloatValue("confidence"),
                    result.getString("reason"),
                    result.getString("suggestion")
            );
        } catch (Exception e) {
            log.warn("答案自评失败: {}", e.getMessage());
            return new SafetyCheckResult(false, false, 0.8f, null, null);
        }
    }

    private String loadTemplate(String name) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/" + name + ".txt");
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "{{answer}}";
        }
    }

    /**
     * 安全检查结果
     */
    public record SafetyCheckResult(
            boolean hasSafetyRisk,
            boolean isEmergency,
            float confidence,
            String reason,
            String suggestion
    ) {}
}
