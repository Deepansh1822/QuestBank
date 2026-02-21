package in.sfp.main.dto;

import in.sfp.main.enums.DifficultyType;
import in.sfp.main.enums.InputType;
import in.sfp.main.enums.QuestionType;
import in.sfp.main.models.Questions;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuestionRequest {
    // Metadata (Common for all questions in this set)
    private String organisation;
    private String className;
    private String subjectName;
    private String chapterName;
    private String topicName;
    private QuestionType questionType;
    private DifficultyType difficultyType;
    private Integer questionMarks;
    private InputType inputType;
    private Integer numberOfQuestions;
    private String instructions;
    private String examType;
    private String totalTime;
    private String setNumber;

    // List of individual questions
    private List<Questions> questions = new ArrayList<>();

    public QuestionRequest() {
    }
}
