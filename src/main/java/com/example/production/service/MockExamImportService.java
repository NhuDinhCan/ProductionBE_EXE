package com.example.production.service;

import com.example.production.dto.AdminImportResult;
import com.example.production.entity.MockExam;
import com.example.production.entity.MockQuestion;
import com.example.production.exception.AppException;
import com.example.production.repositpry.MockExamRepository;
import com.example.production.repositpry.MockQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockExamImportService {

    private static final String ACTIVE = "ACTIVE";
    private static final Set<String> OPTIONS = Set.of("A", "B", "C", "D");
    private static final List<String> QUESTION_HEADERS = List.of(
            "order_index", "question_text", "option_a", "option_b", "option_c", "option_d");
    private static final List<String> ANSWER_HEADERS = List.of(
            "order_index", "correct_option", "explanation");

    private final MockExamRepository mockExamRepository;
    private final MockQuestionRepository mockQuestionRepository;

    @Transactional
    public AdminImportResult uploadQuestions(Long examId, MultipartFile file) {
        MockExam exam = findExam(examId);
        List<String> errors = new ArrayList<>();
        List<QuestionImportRow> rows = readQuestionRows(file, errors);

        Set<Integer> existingOrderIndexes = mockQuestionRepository.findByExamIdOrderByOrderIndexAscIdAsc(examId).stream()
                .map(MockQuestion::getOrderIndex)
                .filter(index -> index != null)
                .collect(Collectors.toSet());

        for (QuestionImportRow row : rows) {
            if (existingOrderIndexes.contains(row.orderIndex())) {
                errors.add("Dòng " + row.rowNumber() + ": order_index " + row.orderIndex() + " đã tồn tại trong đề này");
            }
        }

        if (!errors.isEmpty()) {
            return importResult(false, rows.size(), 0, 0, errors, List.of());
        }

        List<MockQuestion> questions = rows.stream()
                .map(row -> MockQuestion.builder()
                        .exam(exam)
                        .orderIndex(row.orderIndex())
                        .questionText(row.questionText())
                        .imageUrl(row.imageUrl())
                        .optionA(row.optionA())
                        .optionB(row.optionB())
                        .optionC(row.optionC())
                        .optionD(row.optionD())
                        .correctOption("?")
                        .build())
                .toList();
        List<MockQuestion> savedQuestions = mockQuestionRepository.saveAll(questions);

        return importResult(true, rows.size(), savedQuestions.size(), 0, List.of(),
                savedQuestions.stream().map(this::questionMap).toList());
    }

    @Transactional
    public AdminImportResult uploadAnswers(Long examId, MultipartFile file) {
        findExam(examId);
        List<String> errors = new ArrayList<>();
        List<AnswerImportRow> rows = readAnswerRows(file, errors);

        List<MockQuestion> questions = mockQuestionRepository.findByExamIdOrderByOrderIndexAscIdAsc(examId);
        Map<Integer, MockQuestion> questionByOrderIndex = questions.stream()
                .filter(question -> question.getOrderIndex() != null)
                .collect(Collectors.toMap(MockQuestion::getOrderIndex, Function.identity(), (left, right) -> left));

        if (questions.isEmpty()) {
            errors.add("Đề thi chưa có câu hỏi. Vui lòng upload file đề trước.");
        }

        Set<Integer> answerOrderIndexes = rows.stream().map(AnswerImportRow::orderIndex).collect(Collectors.toSet());
        for (AnswerImportRow row : rows) {
            if (!questionByOrderIndex.containsKey(row.orderIndex())) {
                errors.add("Dòng " + row.rowNumber() + ": order_index " + row.orderIndex() + " không tồn tại trong đề thi");
            }
        }

        List<Integer> missingAnswers = questionByOrderIndex.keySet().stream()
                .filter(orderIndex -> !answerOrderIndexes.contains(orderIndex))
                .sorted()
                .toList();
        if (!missingAnswers.isEmpty()) {
            errors.add("File đáp án thiếu order_index: " + missingAnswers);
        }

        if (!errors.isEmpty()) {
            return importResult(false, rows.size(), 0, 0, errors, List.of());
        }

        for (AnswerImportRow row : rows) {
            MockQuestion question = questionByOrderIndex.get(row.orderIndex());
            question.setCorrectOption(row.correctOption());
            question.setExplanation(row.explanation());
        }
        List<MockQuestion> savedQuestions = mockQuestionRepository.saveAll(questionByOrderIndex.values());

        return importResult(true, rows.size(), 0, rows.size(), List.of(),
                savedQuestions.stream()
                        .sorted((left, right) -> Integer.compare(
                                left.getOrderIndex() == null ? Integer.MAX_VALUE : left.getOrderIndex(),
                                right.getOrderIndex() == null ? Integer.MAX_VALUE : right.getOrderIndex()))
                        .map(this::questionMap)
                        .toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> previewQuestions(Long examId) {
        findExam(examId);
        return mockQuestionRepository.findByExamIdOrderByOrderIndexAscIdAsc(examId).stream()
                .map(this::questionMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> publish(Long examId) {
        MockExam exam = findExam(examId);
        List<MockQuestion> questions = mockQuestionRepository.findByExamIdOrderByOrderIndexAscIdAsc(examId);
        if (questions.isEmpty()) {
            throw AppException.badRequest("Không thể publish vì đề thi chưa có câu hỏi");
        }

        List<Integer> invalidAnswerIndexes = questions.stream()
                .filter(question -> !OPTIONS.contains(normalizeUpper(question.getCorrectOption())))
                .map(question -> question.getOrderIndex() == null ? -1 : question.getOrderIndex())
                .sorted()
                .toList();
        if (!invalidAnswerIndexes.isEmpty()) {
            throw AppException.badRequest("Không thể publish vì thiếu đáp án đúng ở order_index: " + invalidAnswerIndexes);
        }

        exam.setStatus(ACTIVE);
        MockExam savedExam = mockExamRepository.save(exam);
        return examMap(savedExam, questions.size());
    }

    private List<QuestionImportRow> readQuestionRows(MultipartFile file, List<String> errors) {
        List<QuestionImportRow> rows = new ArrayList<>();
        List<ExcelRow> excelRows = readRows(file, QUESTION_HEADERS, errors);
        if (excelRows.isEmpty() && errors.isEmpty()) {
            errors.add("File Excel chưa có dòng dữ liệu");
        }
        for (ExcelRow row : excelRows) {
            Integer orderIndex = parseOrderIndex(row, errors);
            String questionText = requiredCell(row, "question_text", "question_text", errors);
            String imageUrl = optionalCell(row, "image_url");
            String optionA = requiredCell(row, "option_a", "option_a", errors);
            String optionB = requiredCell(row, "option_b", "option_b", errors);
            String optionC = requiredCell(row, "option_c", "option_c", errors);
            String optionD = requiredCell(row, "option_d", "option_d", errors);
            if (orderIndex != null && questionText != null && optionA != null && optionB != null && optionC != null && optionD != null) {
                rows.add(new QuestionImportRow(row.rowNumber(), orderIndex, questionText, imageUrl, optionA, optionB, optionC, optionD));
            }
        }
        validateDuplicateOrderIndexes(rows.stream().map(row -> new IndexedRow(row.rowNumber(), row.orderIndex())).toList(), errors);
        return rows;
    }

    private List<AnswerImportRow> readAnswerRows(MultipartFile file, List<String> errors) {
        List<AnswerImportRow> rows = new ArrayList<>();
        List<ExcelRow> excelRows = readRows(file, ANSWER_HEADERS, errors);
        if (excelRows.isEmpty() && errors.isEmpty()) {
            errors.add("File Excel chưa có dòng dữ liệu");
        }
        for (ExcelRow row : excelRows) {
            Integer orderIndex = parseOrderIndex(row, errors);
            String correctOption = normalizeUpper(requiredCell(row, "correct_option", "correct_option", errors));
            if (StringUtils.hasText(correctOption) && !OPTIONS.contains(correctOption)) {
                errors.add("Dòng " + row.rowNumber() + ": correct_option chỉ được là A, B, C hoặc D");
            }
            String explanation = optionalCell(row, "explanation");
            if (orderIndex != null && OPTIONS.contains(correctOption)) {
                rows.add(new AnswerImportRow(row.rowNumber(), orderIndex, correctOption, explanation));
            }
        }
        validateDuplicateOrderIndexes(rows.stream().map(row -> new IndexedRow(row.rowNumber(), row.orderIndex())).toList(), errors);
        return rows;
    }

    private List<ExcelRow> readRows(MultipartFile file, List<String> requiredHeaders, List<String> errors) {
        if (file == null || file.isEmpty()) {
            errors.add("File không được để trống");
            return List.of();
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                errors.add("File Excel không có sheet dữ liệu");
                return List.of();
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add("File Excel thiếu dòng header");
                return List.of();
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> headerIndexes = new HashMap<>();
            for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
                String header = normalizeHeader(formatter.formatCellValue(headerRow.getCell(cellIndex)));
                if (StringUtils.hasText(header)) {
                    headerIndexes.put(header, cellIndex);
                }
            }

            List<String> missingHeaders = requiredHeaders.stream()
                    .filter(header -> !headerIndexes.containsKey(header))
                    .toList();
            if (!missingHeaders.isEmpty()) {
                errors.add("File Excel thiếu cột: " + missingHeaders);
                return List.of();
            }

            List<ExcelRow> rows = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row sheetRow = sheet.getRow(rowIndex);
                if (sheetRow == null || isBlankRow(sheetRow, headerIndexes.values(), formatter)) {
                    continue;
                }

                Map<String, String> values = new HashMap<>();
                for (String header : headerIndexes.keySet()) {
                    values.put(header, formatter.formatCellValue(sheetRow.getCell(headerIndexes.get(header))).trim());
                }
                rows.add(new ExcelRow(rowIndex + 1, values));
            }
            return rows;
        } catch (IOException ex) {
            errors.add("Không đọc được file Excel: " + ex.getMessage());
            return List.of();
        } catch (Exception ex) {
            errors.add("File Excel không hợp lệ. Vui lòng dùng template .xlsx chuẩn.");
            return List.of();
        }
    }

    private boolean isBlankRow(Row row, Iterable<Integer> cellIndexes, DataFormatter formatter) {
        for (Integer cellIndex : cellIndexes) {
            if (StringUtils.hasText(formatter.formatCellValue(row.getCell(cellIndex)))) {
                return false;
            }
        }
        return true;
    }

    private Integer parseOrderIndex(ExcelRow row, List<String> errors) {
        String raw = requiredCell(row, "order_index", "order_index", errors);
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            double value = Double.parseDouble(raw.replace(",", "."));
            if (value <= 0 || value % 1 != 0) {
                errors.add("Dòng " + row.rowNumber() + ": order_index phải là số nguyên dương");
                return null;
            }
            return (int) value;
        } catch (NumberFormatException ex) {
            errors.add("Dòng " + row.rowNumber() + ": order_index phải là số nguyên");
            return null;
        }
    }

    private void validateDuplicateOrderIndexes(List<IndexedRow> rows, List<String> errors) {
        Set<Integer> seen = new HashSet<>();
        for (IndexedRow row : rows) {
            if (!seen.add(row.orderIndex())) {
                errors.add("Dòng " + row.rowNumber() + ": order_index " + row.orderIndex() + " bị trùng trong file");
            }
        }
    }

    private String requiredCell(ExcelRow row, String key, String label, List<String> errors) {
        String value = optionalCell(row, key);
        if (!StringUtils.hasText(value)) {
            errors.add("Dòng " + row.rowNumber() + ": " + label + " không được để trống");
            return null;
        }
        return value;
    }

    private String optionalCell(ExcelRow row, String key) {
        String value = row.values().get(key);
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeHeader(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String normalizeUpper(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private MockExam findExam(Long examId) {
        return mockExamRepository.findById(examId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy đề thi"));
    }

    private AdminImportResult importResult(
            boolean success,
            int totalRows,
            int importedCount,
            int updatedCount,
            List<String> errors,
            List<Map<String, Object>> items) {
        return AdminImportResult.builder()
                .success(success)
                .totalRows(totalRows)
                .importedCount(importedCount)
                .updatedCount(updatedCount)
                .errors(errors)
                .items(items)
                .build();
    }

    private Map<String, Object> questionMap(MockQuestion question) {
        String correctOption = normalizeUpper(question.getCorrectOption());
        return mapOf("id", question.getId(), "examId", question.getExam().getId(),
                "orderIndex", question.getOrderIndex(), "questionText", question.getQuestionText(),
                "imageUrl", question.getImageUrl(),
                "optionA", question.getOptionA(), "optionB", question.getOptionB(),
                "optionC", question.getOptionC(), "optionD", question.getOptionD(),
                "correctOption", OPTIONS.contains(correctOption) ? correctOption : null,
                "explanation", question.getExplanation());
    }

    private Map<String, Object> examMap(MockExam exam, int totalQuestions) {
        return mapOf("id", exam.getId(), "title", exam.getTitle(), "status", exam.getStatus(),
                "durationMinutes", exam.getDurationMinutes(), "totalQuestions", totalQuestions);
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put((String) values[i], values[i + 1]);
        }
        return map;
    }

    private record ExcelRow(int rowNumber, Map<String, String> values) {
    }

    private record QuestionImportRow(
            int rowNumber,
            Integer orderIndex,
            String questionText,
            String imageUrl,
            String optionA,
            String optionB,
            String optionC,
            String optionD) {
    }

    private record AnswerImportRow(int rowNumber, Integer orderIndex, String correctOption, String explanation) {
    }

    private record IndexedRow(int rowNumber, Integer orderIndex) {
    }
}
