package in.sfp.main.service;

import in.sfp.main.models.Questions;
import java.util.List;

public interface QuestionService {

    public List<Questions> addQuestions(List<Questions> questions);

    public long getTotalQuestionCount();

    public long getOrganisationCount();

    public long getSubjectCount();

    public List<String> getDistinctOrganisations();

    public List<String> getDistinctClasses();

    public List<String> getDistinctSubjects();

    public List<String> getDistinctChapters();

    public List<String> getDistinctTopics();

    public List<Questions> getRandomQuestions(String className, String subjectName,
            String chapterName, String topicName, in.sfp.main.enums.QuestionType type,
            in.sfp.main.enums.DifficultyType difficulty, int count);

    public List<Questions> searchQuestions(String keyword);
}
