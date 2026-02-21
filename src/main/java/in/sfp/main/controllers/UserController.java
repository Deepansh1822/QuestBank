package in.sfp.main.controllers;

import in.sfp.main.models.User;
import in.sfp.main.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> data, Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();

        var user = userRepository.findByEmail(principal.getName()).orElseThrow();

        if (data.containsKey("firstName"))
            user.setFirstName(data.get("firstName"));
        if (data.containsKey("lastName"))
            user.setLastName(data.get("lastName"));
        if (data.containsKey("profileImage"))
            user.setProfileImage(data.get("profileImage"));

        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
