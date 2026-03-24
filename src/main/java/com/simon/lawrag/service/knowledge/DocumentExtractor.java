package com.simon.lawrag.service.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文档文本提取器
 * 支持 PDF、Word(doc/docx)、TXT、Excel(xlsx) 格式
 */
@Slf4j
@Component
public class DocumentExtractor {

    /**
     * 按页提取 PDF 文本
     * @return List<PageContent>  每个元素含页码和该页文本
     */
    public List<PageContent> extractPdf(InputStream inputStream) {
        List<PageContent> pages = new ArrayList<>();
//        try (PDDocument document = PDDocument.load(inputStream)) {
            try (PDDocument document =       Loader.loadPDF(new RandomAccessReadBuffer(inputStream))){
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();
            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String text = stripper.getText(document).trim();
                if (!text.isEmpty()) {
                    pages.add(new PageContent(i, text));
                }
            }
            log.info("PDF提取完成: {}页, {}个有效页", totalPages, pages.size());
        } catch (IOException e) {
            log.error("PDF提取失败", e);
            throw new RuntimeException("PDF解析失败: " + e.getMessage());
        }
        return pages;
    }

    /**
     * 提取 Word(docx) 全文
     */
    public String extractDocx(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = normalizeExtractedText(extractor.getText());
            log.info("DOCX提取完成: {}字符", text.length());
            return text;
        } catch (IOException e) {
            log.error("DOCX提取失败", e);
            throw new RuntimeException("Word文档解析失败: " + e.getMessage());
        }
    }

    /**
     * 提取 Word(doc) 全文（OLE2 格式）
     */
    public String extractDoc(InputStream inputStream) {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            // 不同来源的 .doc 文本提取效果差异较大，取“清洗后最长文本”作为结果
            String fromExtractor = normalizeExtractedText(extractor.getText());
            String fromRange = normalizeExtractedText(document.getRange() != null ? document.getRange().text() : "");
            String fromParagraphs = normalizeExtractedText(String.join("\n", Arrays.asList(extractor.getParagraphText())));

            String text = fromExtractor;
            if (fromRange.length() > text.length()) text = fromRange;
            if (fromParagraphs.length() > text.length()) text = fromParagraphs;

            log.info("DOC提取完成: {}字符", text.length());
            return text;
        } catch (IOException e) {
            log.error("DOC提取失败", e);
            throw new RuntimeException("Word文档解析失败: " + e.getMessage());
        }
    }

    /**
     * 提取 TXT 全文
     */
    public String extractTxt(InputStream inputStream) {
        try {
            return normalizeExtractedText(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("TXT提取失败", e);
            throw new RuntimeException("文本文件读取失败: " + e.getMessage());
        }
    }

    /**
     * 提取 Excel(xlsx) 全文（按 sheet/行拼接）
     */
    public String extractXlsx(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                if (sheet == null) {
                    continue;
                }
                sb.append("[Sheet] ").append(sheet.getSheetName()).append("\n");

                Row headerRow = findFirstNonEmptyRow(sheet, formatter);
                List<String> headers = headerRow != null ? readRowValues(headerRow, formatter) : List.of();
                int startRowNum = headerRow != null ? headerRow.getRowNum() + 1 : sheet.getFirstRowNum();

                for (int rowNum = startRowNum; rowNum <= sheet.getLastRowNum(); rowNum++) {
                    Row row = sheet.getRow(rowNum);
                    if (row == null) {
                        continue;
                    }
                    List<String> values = readRowValues(row, formatter);
                    if (values.stream().allMatch(String::isBlank)) {
                        continue;
                    }

                    StringBuilder rowText = new StringBuilder();
                    rowText.append("行").append(rowNum + 1).append(": ");
                    for (int i = 0; i < values.size(); i++) {
                        String value = values.get(i);
                        if (value.isBlank()) {
                            continue;
                        }
                        String key;
                        if (i < headers.size() && !headers.get(i).isBlank()) {
                            key = headers.get(i);
                        } else {
                            key = "列" + (i + 1);
                        }
                        if (rowText.length() > 4) {
                            rowText.append(" | ");
                        }
                        rowText.append(key).append("=").append(value);
                    }
                    if (rowText.length() > 4) {
                        sb.append(rowText).append("\n");
                    }
                }
                sb.append("\n");
            }
            String text = normalizeExtractedText(sb.toString());
            log.info("XLSX提取完成: {}字符", text.length());
            return text;
        } catch (IOException e) {
            log.error("XLSX提取失败", e);
            throw new RuntimeException("Excel文档解析失败: " + e.getMessage());
        }
    }

    private Row findFirstNonEmptyRow(Sheet sheet, DataFormatter formatter) {
        for (int rowNum = sheet.getFirstRowNum(); rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                continue;
            }
            List<String> values = readRowValues(row, formatter);
            if (values.stream().anyMatch(v -> !v.isBlank())) {
                return row;
            }
        }
        return null;
    }

    private List<String> readRowValues(Row row, DataFormatter formatter) {
        int lastCell = Math.max(row.getLastCellNum(), 0);
        List<String> values = new ArrayList<>(lastCell);
        for (int i = 0; i < lastCell; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String value = cell == null ? "" : formatter.formatCellValue(cell).trim();
            values.add(value);
        }
        return values;
    }

    /**
     * 根据文件类型自动选择提取方式
     * @return 全文字符串（PDF按页拼接）
     */
    public String extract(InputStream inputStream, String fileType) {
        final byte[] raw;
        try {
            raw = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败: " + e.getMessage(), e);
        }

        return switch (fileType.toLowerCase()) {
            case "pdf" -> {
                List<PageContent> pages = extractPdf(new ByteArrayInputStream(raw));
                StringBuilder sb = new StringBuilder();
                for (PageContent p : pages) {
                    sb.append(p.text()).append("\n\n");
                }
                yield sb.toString();
            }
            case "docx" -> {
                try {
                    yield extractDocx(new ByteArrayInputStream(raw));
                } catch (RuntimeException ex) {
                    // 部分历史文件扩展名与真实格式不一致：docx 扩展名但实际是 OLE2(doc)
                    if (ex.getMessage() != null && ex.getMessage().contains("OLE2")) {
                        log.warn("检测到 OLE2 内容，docx 自动降级为 doc 解析");
                        yield extractDoc(new ByteArrayInputStream(raw));
                    }
                    throw ex;
                }
            }
            case "doc" -> {
                try {
                    yield extractDoc(new ByteArrayInputStream(raw));
                } catch (RuntimeException ex) {
                    // 反向兜底：doc 扩展名但实际是 OOXML(docx)
                    log.warn("doc 解析失败，尝试按 docx 解析: {}", ex.getMessage());
                    yield extractDocx(new ByteArrayInputStream(raw));
                }
            }
            case "txt" -> extractTxt(new ByteArrayInputStream(raw));
            case "xlsx" -> extractXlsx(new ByteArrayInputStream(raw));
            default -> throw new RuntimeException("不支持的文件格式: " + fileType);
        };
    }

    /**
     * 清理提取文本中的控制字符，避免出现“看似有长度但实际为空白/控制符”的情况
     */
    private String normalizeExtractedText(String text) {
        if (text == null || text.isEmpty()) return "";
        String normalized = text
                .replace('\u0007', ' ')
                .replace('\u000B', ' ')
                .replace('\u000C', ' ')
                .replace('\u001E', ' ')
                .replace('\u001F', ' ')
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ")
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{2,}", "\n")
                .trim();
        return normalized;
    }

    /**
     * 页面内容记录
     */
    public record PageContent(int pageNumber, String text) {}
}
