package in.sfp.main.service;

import in.sfp.main.config.JwtService;
import in.sfp.main.dto.AuthenticationRequest;
import in.sfp.main.dto.AuthenticationResponse;
import in.sfp.main.dto.ResetPasswordRequest;
import in.sfp.main.dto.RegisterRequest;
import in.sfp.main.models.User;
import in.sfp.main.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

        private final UserRepository repository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public AuthenticationResponse register(RegisterRequest request) {
                // Check if email already exists
                if (repository.findByEmail(request.getEmail()).isPresent()) {
                        throw new RuntimeException("Email already exists");
                }

                var user = User.builder()
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .secretKey(request.getSecretKey()) // Store the secret key
                                .role("USER")
                                .build();
                repository.save(user);
                var jwtToken = jwtService.generateToken(user);
                // No token usage in User model currently other than for auth response
                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .build();
        }

        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                // First authenticate using the manager (checks password)
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                // If we get here, password is correct
                var user = repository.findByEmail(request.getEmail())
                                .orElseThrow();

                // Generate a fresh token
                var jwtToken = jwtService.generateToken(user);

                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .build();
        }

        public void resetPassword(ResetPasswordRequest request) {
                var user = repository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                String storedKey = user.getSecretKey();
                String providedKey = request.getSecretKey();

                // Trim whitespace and check for null/empty
                if (storedKey == null || providedKey == null ||
                                storedKey.trim().isEmpty() || !storedKey.trim().equals(providedKey.trim())) {
                        throw new RuntimeException("Invalid Secret Key provided for this account.");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                repository.save(user);
        }
}
