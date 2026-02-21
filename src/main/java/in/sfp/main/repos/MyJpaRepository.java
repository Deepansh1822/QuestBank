package in.sfp.main.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import in.sfp.main.models.Questions;
import in.sfp.main.enums.QuestionType;
import in.sfp.main.enums.DifficultyType;
import java.util.Optional;
import java.util.List;

@Repository
public interface MyJpaRepository extends JpaRepository<Questions, Integer> {

        Optional<Questions> findByQuestionAndClassNameAndSubjectNameAndQuestionTypeAndDifficultyTypeAndOrganisation(
                        String question, String className, String subjectName, QuestionType questionType,
                        DifficultyType difficultyType, String organisation);

        @Query("SELECT COUNT(DISTINCT q.organisation) FROM Questions q")
        long countDistinctOrganisation();

        @Query("SELECT COUNT(DISTINCT q.subjectName) FROM Questions q")
        long countDistinctSubjectName();

        @Query("SELECT DISTINCT q.organisation FROM Questions q")
        List<String> findDistinctOrganisation(Pageable pageable);

        @Query("SELECT DISTINCT q.className FROM Questions q")
        List<String> findDistinctClassName();

        @Query("SELECT DISTINCT q.subjectName FROM Questions q")
        List<String> findDistinctSubjectName();

        @Query("SELECT DISTINCT q.chapterName FROM Questions q")
        List<String> findDistinctChapterName();

        @Query("SELECT DISTINCT q.topicName FROM Questions q")
        List<String> findDistinctTopicName();

        @Query("SELECT q FROM Questions q WHERE q.className = :className AND q.subjectName = :subjectName AND q.chapterName = :chapterName AND q.topicName = :topicName AND q.questionType = :questionType AND q.difficultyType = :difficultyType")
        List<Questions> findByClassNameAndSubjectNameAndChapterNameAndTopicNameAndQuestionTypeAndDifficultyType(
                        String className, String subjectName, String chapterName, String topicName,
                        QuestionType questionType,
                        DifficultyType difficultyType);

        @Query("SELECT q FROM Questions q WHERE LOWER(q.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(q.topicName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(q.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(q.chapterName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<Questions> findByKeyword(String keyword, Pageable pageable);

}
