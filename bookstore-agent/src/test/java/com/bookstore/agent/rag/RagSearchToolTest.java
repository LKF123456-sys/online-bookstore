package com.bookstore.agent.rag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RagSearchTool 单元测试 — 验证 RAG 语义搜索逻辑
 *
 * 面试亮点：
 *   1. Mock VectorStore 隔离外部依赖（实际是内存实现，但 Mock 更快）
 *   2. 验证搜索结果格式化（LLM 消费的是文本，格式质量影响回复质量）
 *   3. 空结果和异常场景的优雅降级
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RagSearchTool RAG 语义搜索测试")
class RagSearchToolTest {

    @Mock
    private VectorStore vectorStore;

    private RagSearchTool ragSearchTool;

    @BeforeEach
    void setUp() {
        ragSearchTool = new RagSearchTool(vectorStore);
    }

    @Nested
    @DisplayName("语义搜索图书")
    class SemanticSearchTests {

        @Test
        @DisplayName("正常搜索 — 返回格式化结果")
        void shouldReturnFormattedResults() {
            Document doc1 = new Document(
                    "书名：三体\n作者：刘慈欣\n分类：科幻\n简介：地球文明与三体文明的宇宙史诗",
                    Map.of("type", "product", "productId", "PROD-001")
            );
            Document doc2 = new Document(
                    "书名：银河帝国\n作者：阿西莫夫\n分类：科幻\n简介：人类银河殖民史",
                    Map.of("type", "product", "productId", "PROD-002")
            );

            when(vectorStore.similaritySearch(any(SearchRequest.class)))
                    .thenReturn(List.of(doc1, doc2));

            String result = ragSearchTool.semanticSearchBooks("好看的科幻小说", 5);

            assertNotNull(result);
            assertTrue(result.contains("三体"));
            assertTrue(result.contains("银河帝国"));
            assertTrue(result.contains("语义分析"));
            verify(vectorStore).similaritySearch(any(SearchRequest.class));
        }

        @Test
        @DisplayName("空结果 — 返回友好提示")
        void shouldHandleEmptyResults() {
            when(vectorStore.similaritySearch(any(SearchRequest.class)))
                    .thenReturn(List.of());

            String result = ragSearchTool.semanticSearchBooks("不存在的主题", 5);

            assertTrue(result.contains("未找到"));
        }

        @Test
        @DisplayName("VectorStore 异常 — 优雅降级")
        void shouldHandleVectorStoreException() {
            when(vectorStore.similaritySearch(any(SearchRequest.class)))
                    .thenThrow(new RuntimeException("VectorStore unavailable"));

            String result = ragSearchTool.semanticSearchBooks("测试", 5);

            assertTrue(result.contains("错误"));
            assertTrue(result.contains("VectorStore unavailable"));
        }
    }

    @Nested
    @DisplayName("知识库搜索")
    class KnowledgeSearchTests {

        @Test
        @DisplayName("搜索领域知识 — 返回知识片段")
        void shouldReturnKnowledgeChunks() {
            Document doc = new Document(
                    "科幻小说入门推荐：《三体》（刘慈欣）、《银河帝国》（阿西莫夫）、《沙丘》（赫伯特）",
                    Map.of("source", "BookCategoryKnowledge.md", "type", "knowledge")
            );

            when(vectorStore.similaritySearch(any(SearchRequest.class)))
                    .thenReturn(List.of(doc));

            String result = ragSearchTool.searchKnowledge("科幻小说有哪些经典");

            assertTrue(result.contains("领域知识"));
            assertTrue(result.contains("三体"));
            assertTrue(result.contains("BookCategoryKnowledge.md"));
        }

        @Test
        @DisplayName("空知识库 — 返回提示")
        void shouldHandleEmptyKnowledge() {
            when(vectorStore.similaritySearch(any(SearchRequest.class)))
                    .thenReturn(List.of());

            String result = ragSearchTool.searchKnowledge("冷门主题");

            assertTrue(result.contains("暂无"));
        }
    }
}
