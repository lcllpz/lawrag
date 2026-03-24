package com.simon.lawrag.service.rag;

import lombok.Data;

/**
 * 检索结果切片
 */
@Data
public class RetrievedChunk {

    private String id;

    /** 切片内容 */
    private String content;

    /** 相关性得分（越高越相关）*/
    private float score;

    /** 科室分类 */
    private String category;

    /** 内容类型：definition/symptom/treatment/drug/general */
    private String contentType;

    /** 章节名 */
    private String chapter;

    /** 页码 */
    private int pageNumber;

    /** 知识库 ID */
    private Long knowledgeBaseId;

    /** 知识库文档名（join 后填入）*/
    private String sourceName;

    /** 来源标注文本（用于前端显示）*/
    private String sourceRef;

    /** 检索来源标记 */
    private Source source;

    /** RRF 融合后的排名 */
    private int rrfRank;

    /** Cross-Encoder 重排序得分 */
    private float rerankScore;

    public enum Source {
        VECTOR, BM25, HYBRID
    }
}
