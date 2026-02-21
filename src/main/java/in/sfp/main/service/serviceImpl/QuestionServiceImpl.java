package in.sfp.main.service.serviceImpl;

import in.sfp.main.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import in.sfp.main.models.Questions;
import in.sfp.main.repos.MyJpaRepository;
import in.sfp.main.service.QuestionService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private MyJpaRepository myJpaRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Override
    public List<Questions> addQuestions(List<Questions> questions) {
        for (Questions q : questions) {
            java.util.Optional<Questions> existing = myJpaRepository
                    .findByQuestionAndClassNameAndSubjectNameAndQuestionTypeAndDifficultyTypeAndOrganisation(
                            q.getQuestion(), q.getClassName(), q.getSubjectName(), q.getQuestionType(),
                            q.getDifficultyType(), q.getOrganisation());

            if (existing.isPresent()) {
                // If question exists with same context, update the ID to perform an update
                // instead of insert
                q.setQid(existing.get().getQid());
            }
        }
        return myJpaRepository.saveAll(questions);
    }

    @Override
    public long getTotalQuestionCount() {
        return myJpaRepository.count();
    }

    @Override
    public long getOrganisationCount() {
        return myJpaRepository.countDistinctOrganisation();
    }

    @Override
    public long getSubjectCount() {
        return myJpaRepository.countDistinctSubjectName();
    }

    @Override
    public List<String> getDistinctOrganisations() {
        List<String> fromQuestions = myJpaRepository
                .findDistinctOrganisation(org.springframework.data.domain.PageRequest.of(0, 50));
        List<String> fromOrgTable = organisationRepository.findAll().stream()
                .map(in.sfp.main.models.Organisation::getName)
                .collect(Collectors.toList());

        return Stream.concat(fromQuestions.stream(), fromOrgTable.stream())
                .filter(s -> s != null && !s.trim().isEmpty())
                .distinct()
                .limit(50)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getDistinctClasses() {
        return myJpaRepository.findDistinctClassName();
    }

    @Override
    public List<String> getDistinctSubjects() {
        return myJpaRepository.findDistinctSubjectName();
    }

    @Override
    public List<String> getDistinctChapters() {
        return myJpaRepository.findDistinctChapterName();
    }

    @Override
    public List<String> getDistinctTopics() {
        return myJpaRepository.findDistinctTopicName();
    }

    @Override
    public List<Questions> getRandomQuestions(String className, String subjectName,
            String chapterName, String topicName, in.sfp.main.enums.QuestionType type,
            in.sfp.main.enums.DifficultyType difficulty, int count) {
        List<Questions> allMatching = myJpaRepository
                .findByClassNameAndSubjectNameAndChapterNameAndTopicNameAndQuestionTypeAndDifficultyType(
                        className, subjectName, chapterName, topicName, type, difficulty);

        if (allMatching.isEmpty()) {
            return allMatching;
        }

        java.util.Collections.shuffle(allMatching);

        return allMatching.stream().limit(count).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Questions> searchQuestions(String keyword) {
        return myJpaRepository.findByKeyword(keyword, org.springframework.data.domain.PageRequest.of(0, 10));
    }
}
