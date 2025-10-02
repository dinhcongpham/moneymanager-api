package com.example.moneymanager.service;

import com.example.moneymanager.common.BadRequestException;
import com.example.moneymanager.common.UnauthorizedException;
import com.example.moneymanager.dto.AuthDto;
import com.example.moneymanager.dto.ProfileDto;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.ProfileRepository;
import com.example.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationURL;

    public ProfileDto registerProfile(ProfileDto profileDto) {
        if (profileRepository.existsByEmail(profileDto.getEmail())) {
            throw new BadRequestException("Email Already Exists");
        }

        ProfileEntity profileEntity = toEntity(profileDto);
        profileEntity.setActivationToken(UUID.randomUUID().toString());
        profileEntity = profileRepository.save(profileEntity);

        // send activation email
        String activationLink = activationURL + "/api/v1.0/profile/activate?token=" + profileEntity.getActivationToken();
        String subject = "Activate your moneymanager account!";
        String body = "Click on the following link to activate your moneymanager account: " + activationLink;

        emailService.send(profileEntity.getEmail(), subject, body);

        return toDto(profileEntity);
    }

    public Boolean activateProfile(String activateToken) {
        return profileRepository.findByActivationToken(activateToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    public Boolean isAccountActivated(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    public ProfileDto getPublicProfile(String email) {
        ProfileEntity currentUser = null;
        if (email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }

        return toDto(currentUser);
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDto authDto) {
        if (profileRepository.existsByEmail(authDto.getEmail())) {
            if (!isAccountActivated(authDto.getEmail())) {
                throw new BadRequestException("Account is not active, Please activate your account first!");
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword())
            );

            String accessToken = jwtUtil.generateAccessToken(authDto.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(authDto.getEmail());
            return Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "user", getPublicProfile(authDto.getEmail())
            );
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public Map<String, Object> authenticateRefreshToken(String token) {
        try {
            String email = jwtUtil.extractUsername(token);

            if (!profileRepository.existsByEmail(email)) {
                throw new BadRequestException("Invalid refresh token");
            }

            String accessToken = jwtUtil.generateAccessToken(email);
            String refreshToken = jwtUtil.generateRefreshToken(email);
            return Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "user", getPublicProfile(email)
            );
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid refresh token");
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }


    public ProfileEntity toEntity(ProfileDto profileDto) {
        return ProfileEntity.builder()
                .id(profileDto.getId())
                .name(profileDto.getName())
                .email(profileDto.getEmail())
                .password(passwordEncoder.encode(profileDto.getPassword()))
                .profileImageUrl(profileDto.getProfileImageUrl())
                .createdAt(profileDto.getCreatedAt())
                .updatedAt(profileDto.getUpdatedAt())
                .build();
    }

    public ProfileDto toDto(ProfileEntity profileEntity) {
        return ProfileDto.builder()
                .id(profileEntity.getId())
                .name(profileEntity.getName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }
}
