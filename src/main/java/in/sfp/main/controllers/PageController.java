package in.sfp.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import in.sfp.main.dto.ContactRequest;
import in.sfp.main.service.EmailService;
import in.sfp.main.dto.QuestionRequest;
import in.sfp.main.service.QuestionService;
import in.sfp.main.repos.UserRepository;
import in.sfp.main.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @ModelAttribute
    public void addUserToModel(Model model, Principal principal) {
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                model.addAttribute("user", user);
            });
        }
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        try {
            model.addAttribute("totalQuestions", questionService.getTotalQuestionCount());
            model.addAttribute("organisationCount", questionService.getOrganisationCount());
            model.addAttribute("subjectCount", questionService.getSubjectCount());
            model.addAttribute("organisations", questionService.getDistinctOrganisations());
            return "dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load dashboard: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/generator")
    public String generator(Model model) {
        model.addAttribute("formRequest", new QuestionRequest());
        return "index";
    }

    @GetMapping("/create-paper")
    public String createPaper(Model model) {
        model.addAttribute("formRequest", new QuestionRequest());
        return "create-paper";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/help")
    public String help() {
        return "help";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("contactRequest", new ContactRequest());
        return "contact";
    }

    @PostMapping("/contact")
    @ResponseBody
    public ResponseEntity<Map<String, String>> submitContact(@ModelAttribute ContactRequest contactRequest) {
        Map<String, String> response = new HashMap<>();
        try {
            emailService.sendContactEmail(contactRequest);
            response.put("status", "success");
            response.put("message", "Thank you! Your message has been sent successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Failed to send message: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/documentation")
    public String documentation() {
        return "documentation";
    }

    @GetMapping("/support")
    public String support() {
        return "support";
    }

}
