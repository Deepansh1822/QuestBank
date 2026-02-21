package in.sfp.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import in.sfp.main.service.QuestionService;
import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataFetchController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/organisations")
    public List<String> getOrganisations() {
        return questionService.getDistinctOrganisations();
    }

    @GetMapping("/classes")
    public List<String> getClasses() {
        return questionService.getDistinctClasses();
    }

    @GetMapping("/subjects")
    public List<String> getSubjects() {
        return questionService.getDistinctSubjects();
    }

    @GetMapping("/chapters")
    public List<String> getChapters() {
        return questionService.getDistinctChapters();
    }

    @GetMapping("/topics")
    public List<String> getTopics() {
        return questionService.getDistinctTopics();
    }
}
