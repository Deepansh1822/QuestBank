package in.sfp.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import in.sfp.main.dto.PaperGenerationRequest;
import in.sfp.main.dto.QuestionRequest;
import in.sfp.main.dto.SectionRequest;
import in.sfp.main.dto.TopicCriteria;
import in.sfp.main.models.Questions;
import in.sfp.main.service.AiGenerationService;
import in.sfp.main.service.PdfExportService;
import in.sfp.main.service.PdfImportService;
import in.sfp.main.service.QuestionService;
import java.util.List;

@Controller
@RequestMapping("/questions")
public class QuestionsController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private PdfImportService pdfImportService;

    @Autowired
    private AiGenerationService aiGenerationService;

    @PostMapping("/ai-generate")
    public ResponseEntity<?> aiGenerate(@RequestBody QuestionRequest request) {
        try {
            System.out.println("AI Generation Request for: " + request.getTopicName());
            List<Questions> questions = aiGenerationService.generateQuestions(request);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            System.err.println("AI Generation Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body("AI Error: " + e.getMessage());
        }
    }

    @PostMapping("/import-pdf")
    public ResponseEntity<List<Questions>> importPdf(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Importing PDF: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)");
            List<Questions> questions = pdfImportService.extractQuestionsFromPdf(file);
            System.out.println("Extracted " + questions.size() + " questions.");
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveQuestions(@RequestBody QuestionRequest formRequest) {
        try {
            System.out.println("Received Save Request for: " + formRequest.getOrganisation());
            if (formRequest.getQuestions() != null) {
                formRequest.getQuestions().forEach(q -> {
                    q.setOrganisation(formRequest.getOrganisation());
                    q.setClassName(formRequest.getClassName());
                    q.setSubjectName(formRequest.getSubjectName());
                    q.setChapterName(formRequest.getChapterName());
                    q.setTopicName(formRequest.getTopicName());
                    q.setQuestionType(formRequest.getQuestionType());
                    q.setDifficultyType(formRequest.getDifficultyType());
                    q.setQuestionMarks(formRequest.getQuestionMarks());
                    q.setInputType(formRequest.getInputType());
                });
                questionService.addQuestions(formRequest.getQuestions());
            }
            return ResponseEntity.ok("Questions saved successfully to the bank!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Save Error: " + e.getMessage());
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateQuestionPaper(@RequestBody QuestionRequest formRequest) {
        try {
            System.out.println("Received PDF Generation Request...");
            byte[] pdfBytes = pdfExportService.generateQuestionPaper(formRequest);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=QuestionPaper.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("PDF Error: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/generate-from-bank")
    public ResponseEntity<?> generateFromBank(@RequestBody QuestionRequest request) {
        try {
            System.out.println("Generating Paper from Bank for: " + request.getOrganisation());
            List<Questions> randomQuestions = questionService.getRandomQuestions(
                    request.getClassName(),
                    request.getSubjectName(),
                    request.getChapterName(),
                    request.getTopicName(),
                    request.getQuestionType(),
                    request.getDifficultyType(),
                    request.getNumberOfQuestions());

            if (randomQuestions.isEmpty()) {
                return ResponseEntity.badRequest().body("No questions found matching these criteria in the bank.");
            }

            request.setQuestions(randomQuestions);
            byte[] pdfBytes = pdfExportService.generateQuestionPaper(request);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=GeneratedPaper.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Generation Error: " + e.getMessage());
        }
    }

    @PostMapping("/generate-sectioned")
    public ResponseEntity<?> generateSectioned(@RequestBody PaperGenerationRequest request) {
        try {
            System.out.println("Generating Sectioned Paper for: " + request.getOrganisation());
            java.util.List<java.util.List<Questions>> sectionedQuestions = new java.util.ArrayList<>();

            for (SectionRequest sr : request.getSections()) {
                java.util.List<Questions> sectionTotalQs = new java.util.ArrayList<>();

                for (TopicCriteria tc : sr.getTopicCriteria()) {
                    List<Questions> qs = questionService.getRandomQuestions(
                            request.getClassName(),
                            request.getSubjectName(),
                            tc.getChapterName(),
                            tc.getTopicName(),
                            sr.getQuestionType(),
                            sr.getDifficultyType(),
                            tc.getNumberOfQuestions());

                    if (qs.isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body("No questions found for " + sr.getSectionName() + " (Topic: " + tc.getTopicName()
                                        + ")");
                    }
                    sectionTotalQs.addAll(qs);
                }
                sectionedQuestions.add(sectionTotalQs);
            }

            byte[] pdfBytes = pdfExportService.generateMultiSectionPaper(request, sectionedQuestions);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=SectionedPaper.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Sectioned Generation Error: " + e.getMessage());
        }
    }

    @PostMapping("/preview-questions")
    public ResponseEntity<?> previewQuestions(@RequestBody PaperGenerationRequest request) {
        try {
            for (SectionRequest sr : request.getSections()) {
                java.util.List<Questions> sectionTotalQs = new java.util.ArrayList<>();
                for (TopicCriteria tc : sr.getTopicCriteria()) {
                    List<Questions> qs = questionService.getRandomQuestions(
                            request.getClassName(),
                            request.getSubjectName(),
                            tc.getChapterName(),
                            tc.getTopicName(),
                            sr.getQuestionType(),
                            sr.getDifficultyType(),
                            tc.getNumberOfQuestions());
                    sectionTotalQs.addAll(qs);
                }
                sr.setQuestions(sectionTotalQs);
            }
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Preview Error: " + e.getMessage());
        }
    }

    @PostMapping("/generate-reviewed-pdf")
    public ResponseEntity<?> generateReviewedPdf(@RequestBody PaperGenerationRequest request) {
        try {
            java.util.List<java.util.List<Questions>> sectionedQuestions = new java.util.ArrayList<>();
            for (SectionRequest sr : request.getSections()) {
                sectionedQuestions.add(sr.getQuestions());
            }
            byte[] pdfBytes = pdfExportService.generateMultiSectionPaper(request, sectionedQuestions);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ReviewedPaper.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Final PDF Error: " + e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String keyword) {
        return ResponseEntity.ok(questionService.searchQuestions(keyword));
    }
}
