package com.example.moneymanager.service;

import com.example.moneymanager.dto.GoogleUser;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.ProfileRepository;
import com.example.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> getExistUser(String email) {
        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "user", profileService.getPublicProfile(email)
        );
    }

    public Map<String, Object> createNewUser(GoogleUser googleUser) {
        ProfileEntity profileEntity = ProfileEntity.builder()
                .name(removeDiacritics(googleUser.getName()))
                .email(googleUser.getEmail())
                .password(passwordEncoder.encode("something"))
                .profileImageUrl(googleUser.getPictureUrl())
                .isActive(true)
                .build();

        profileEntity = profileRepository.save(profileEntity);

        String accessToken = jwtUtil.generateAccessToken(googleUser.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(googleUser.getEmail());
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "user", profileService.toDto(profileEntity)
        );
    }

    private String removeDiacritics(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }
}
