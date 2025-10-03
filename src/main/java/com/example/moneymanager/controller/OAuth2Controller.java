package com.example.moneymanager.controller;

import com.example.moneymanager.dto.GoogleUser;
import com.example.moneymanager.repository.ProfileRepository;
import com.example.moneymanager.service.AuthService;
import com.example.moneymanager.service.GoogleTokenVerifierService;
import com.example.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        GoogleUser googleUser = googleTokenVerifierService.verify(idToken);

        if (googleUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID token");
        }

        Map<String, Object> user;
        if (profileRepository.existsByEmail(googleUser.getEmail())) {
            user = authService.getExistUser(googleUser.getEmail());
        } else {
            user = authService.createNewUser(googleUser);
        }

        return ResponseEntity.ok(user);
    }
}

