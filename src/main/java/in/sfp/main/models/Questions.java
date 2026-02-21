package in.sfp.main.models;

import in.sfp.main.enums.DifficultyType;
import in.sfp.main.enums.QuestionType;
import in.sfp.main.enums.InputType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Questions")
@Table(name = "Questions")
@Getter
@Setter
public class Questions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qid")
    private Integer qid;

    @Column(name = "organisation")
    private String organisation;

    @Column(name = "className")
    private String className;

    @Column(name = "subjectName")
    private String subjectName;

    @Column(name = "chapterName")
    private String chapterName;

    @Column(name = "topicName")
    private String topicName;

    @Enumerated(EnumType.STRING)
    @Column(name = "questionType")
    private QuestionType questionType; // mcq, short, long, trueFalse, fillInTheBlanks

    @Enumerated(EnumType.STRING)
    @Column(name = "difficultyType")
    private DifficultyType difficultyType; // easy, medium, hard

    @Column(name = "questionMarks")
    private Integer questionMarks;

    @Column(name = "question")
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "inputType")
    private InputType inputType;

    @Column(name = "option1", nullable = true) // only for mcq
    private String option1;

    @Column(name = "option2", nullable = true) // only for mcq
    private String option2;

    @Column(name = "option3", nullable = true) // only for mcq
    private String option3;

    @Column(name = "option4", nullable = true) // only for mcq
    private String option4;

    @Column(name = "answer")
    private String answer;

    public Questions() {

    }

    public Questions(Integer qid, String organisation, String className, String subjectName, String chapterName,
            String topicName,
            QuestionType questionType,
            DifficultyType difficultyType, Integer questionMarks, String question, InputType inputType, String option1,
            String option2,
            String option3,
            String option4, String answer) {
        this.qid = qid;
        this.organisation = organisation;
        this.className = className;
        this.subjectName = subjectName;
        this.chapterName = chapterName;
        this.topicName = topicName;
        this.questionType = questionType;
        this.difficultyType = difficultyType;
        this.questionMarks = questionMarks;
        this.question = question;
        this.inputType = inputType;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.answer = answer;
    }
}
