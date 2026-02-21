package in.sfp.main.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.sfp.main.dto.QuestionRequest;
import in.sfp.main.models.Questions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiGenerationService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public List<Questions> generateQuestions(QuestionRequest request) throws Exception {
        // Safe defaults
        int count = (request.getNumberOfQuestions() != null && request.getNumberOfQuestions() > 0)
                ? request.getNumberOfQuestions()
                : 5;
        String type = (request.getQuestionType() != null) ? request.getQuestionType().toString() : "MCQ";
        String topic = (request.getTopicName() != null) ? request.getTopicName() : "General Knowledge";
        String subject = (request.getSubjectName() != null) ? request.getSubjectName() : "General";
        String className = (request.getClassName() != null) ? request.getClassName() : "All";
        String difficulty = (request.getDifficultyType() != null) ? request.getDifficultyType().toString() : "MEDIUM";

        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("You are a professional educational content creator. ")
                .append("Return ONLY a JSON array of question objects. ")
                .append("Each object must have: question, option1, option2, option3, option4, answer. ")
                .append("RULES FOR TYPES: ");

        if (type.equals("MCQ")) {
            systemPrompt.append(
                    "For MCQ: Provide 4 distinct options. The 'answer' must be the correct option letter (A, B, C, or D). ");
        } else if (type.equals("TRUE_FALSE")) {
            systemPrompt.append(
                    "For TRUE_FALSE: 'option1' must be 'True', 'option2' must be 'False'. Set others to empty string. The 'answer' must be 'True' or 'False'. ");
        } else {
            systemPrompt.append("For ")
                    .append(type)
                    .append(": Set all options to empty strings. The 'answer' MUST be a clear, informative text response (NOT a letter). ");
        }

        String userPrompt = String.format(
                "Generate exactly %d %s questions about the topic '%s' for subject '%s' and class '%s'. " +
                        "Difficulty level: %s. Output only the JSON array.",
                count, type, topic, subject, className, difficulty);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt.toString());

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama-3.3-70b-versatile");
        body.put("messages", List.of(systemMsg, userMsg));
        body.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        System.out.println(">>> START GROQ AI GENERATION <<<");
        try {
            String response = restTemplate.postForObject(GROQ_API_URL, entity, String.class);
            return parseGroqResponse(response);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            String errorMsg = e.getResponseBodyAsString();
            System.err.println("Groq Error (" + e.getStatusCode() + "): " + errorMsg);
            throw new Exception("Groq API Error: " + errorMsg);
        } catch (Exception e) {
            System.err.println("Fatal AI Error: " + e.getMessage());
            throw e;
        }
    }

    private List<Questions> parseGroqResponse(String response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        String jsonText = root.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText()
                .trim();

        System.out.println("Raw AI Response content: " + jsonText);

        // Robust Markdown/Text removal
        if (jsonText.contains("[")) {
            jsonText = jsonText.substring(jsonText.indexOf("["));
        }
        if (jsonText.contains("]")) {
            jsonText = jsonText.substring(0, jsonText.lastIndexOf("]") + 1);
        }

        System.out.println("Cleaned JSON. Creating question list...");
        return mapper.readValue(jsonText, new TypeReference<List<Questions>>() {
        });
    }
}
