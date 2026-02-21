package in.sfp.main.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicCriteria {
    private String chapterName;
    private String topicName;
    private Integer numberOfQuestions;

    public TopicCriteria() {
    }
}
