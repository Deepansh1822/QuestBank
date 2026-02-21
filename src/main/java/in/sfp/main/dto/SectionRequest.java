/** DTO for section-wise question requests */
package in.sfp.main.dto;

import in.sfp.main.enums.DifficultyType;
import in.sfp.main.enums.QuestionType;
import in.sfp.main.models.Questions;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public class SectionRequest {
    private String sectionName; // Section A, Section B, etc.
    private java.util.List<TopicCriteria> topicCriteria = new java.util.ArrayList<>();
    private QuestionType questionType;
    private DifficultyType difficultyType;
    private Integer marksPerQuestion;
    private List<Questions> questions = new ArrayList<>();

    public SectionRequest() {
    }
}
