package com.turkcellcase4.security.controller;

import com.turkcellcase4.common.exception.ResourceNotFoundException;
import com.turkcellcase4.common.exception.ValidationException;
import com.turkcellcase4.security.dto.LoginRequestDTO;
import com.turkcellcase4.security.dto.LoginResponseDTO;
import com.turkcellcase4.security.dto.RegisterRequestDTO;
import com.turkcellcase4.security.dto.RegisterResponseDTO;
import com.turkcellcase4.security.jwt.JwtTokenProvider;
import com.turkcellcase4.user.model.User;
import com.turkcellcase4.user.repository.UserRepository;
import com.turkcellcase4.common.enums.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("Auth register attempt for msisdn: {}", request.getMsisdn());
        
        // MSISDN kontrolü
        if (userRepository.findByMsisdn(request.getMsisdn()).isPresent()) {
            throw new ValidationException("Bu MSISDN zaten kayıtlı: " + request.getMsisdn());
        }
        
        // Yeni kullanıcı oluştur
        User newUser = User.builder()
                .name(request.getName())
                .msisdn(request.getMsisdn())
                .type(request.getUserType() != null ? request.getUserType() : UserType.INDIVIDUAL)
                .currentPlanId(request.getCurrentPlanId())
                .build();
        
        User savedUser = userRepository.save(newUser);
        
        // Token oluştur
        String role = (savedUser.getType() == UserType.CORPORATE) ? "ROLE_ADMIN" : "ROLE_USER";
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getMsisdn())
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        
        return ResponseEntity.ok(RegisterResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .msisdn(savedUser.getMsisdn())
                .role(role)
                .message("Kullanıcı başarıyla kayıt oldu")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Auth login attempt for msisdn: {}", request.getMsisdn());
        if (request.getMsisdn() == null || request.getMsisdn().isBlank()) {
            throw new ValidationException("MSISDN gerekli");
        }

        User user = userRepository.findByMsisdn(request.getMsisdn())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + request.getMsisdn()));

        // Basit rol ataması: CORPORATE -> ROLE_ADMIN, diğerleri -> ROLE_USER
        String role = (user.getType() != null && user.getType().name().equals("CORPORATE")) ? "ROLE_ADMIN" : "ROLE_USER";
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getMsisdn())
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        
        return ResponseEntity.ok(LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .msisdn(user.getMsisdn())
                .role(role)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ValidationException("Geçersiz Authorization header");
        }
        
        String refreshToken = authHeader.substring(7);
        String msisdn = jwtTokenProvider.extractUsername(refreshToken);
        
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new ValidationException("Geçersiz refresh token");
        }
        
        User user = userRepository.findByMsisdn(msisdn)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        
        String role = (user.getType() == UserType.CORPORATE) ? "ROLE_ADMIN" : "ROLE_USER";
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getMsisdn())
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        
        return ResponseEntity.ok(LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .msisdn(user.getMsisdn())
                .role(role)
                .build());
    }
}


