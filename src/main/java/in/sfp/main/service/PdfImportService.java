package in.sfp.main.service;

import in.sfp.main.models.Questions;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfImportService {

    public List<Questions> extractQuestionsFromPdf(MultipartFile file) throws IOException {
        List<Questions> questionsList = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String fullText = stripper.getText(document);

            // Simple logic: Split by lines and look for patterns like "1. ", "Q1.", etc.
            // This is a basic implementation. For complex PDFs, AI is better.
            String[] lines = fullText.split("\\r?\\n");

            Questions currentQuestion = null;
            StringBuilder textBuffer = new StringBuilder();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                // Match "1. " or "Q1:" or "Question 1:"
                if (line.matches("^(?i)(Question\\s*\\d+[:.]|Q\\d+[:.]|\\d+[:.]).*")) {
                    if (currentQuestion != null) {
                        currentQuestion.setQuestion(textBuffer.toString().trim());
                        questionsList.add(currentQuestion);
                    }
                    currentQuestion = new Questions();
                    textBuffer = new StringBuilder();

                    // Remove the "1." prefix from the beginning of the question text
                    String cleanLine = line.replaceFirst("^(?i)(Question\\s*\\d+[:.]|Q\\d+[:.]|\\d+[:.])\\s*", "");
                    textBuffer.append(cleanLine).append(" ");
                } else if (currentQuestion != null) {
                    // Check for MCQ options
                    if (line.matches("^[A-D][).].*")) {
                        String optionText = line.substring(2).trim();
                        if (line.startsWith("A"))
                            currentQuestion.setOption1(optionText);
                        else if (line.startsWith("B"))
                            currentQuestion.setOption2(optionText);
                        else if (line.startsWith("C"))
                            currentQuestion.setOption3(optionText);
                        else if (line.startsWith("D"))
                            currentQuestion.setOption4(optionText);
                    } else if (line.toLowerCase().startsWith("ans:") || line.toLowerCase().startsWith("answer:")) {
                        currentQuestion.setAnswer(line.substring(line.indexOf(":") + 1).trim());
                    } else {
                        textBuffer.append(line).append(" ");
                    }
                }
            }

            // Add the last question
            if (currentQuestion != null) {
                currentQuestion.setQuestion(textBuffer.toString().trim());
                questionsList.add(currentQuestion);
            }
        }

        return questionsList;
    }
}
