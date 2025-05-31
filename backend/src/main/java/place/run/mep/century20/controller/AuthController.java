package place.run.mep.century20.controller;

import place.run.mep.century20.dto.LoginRequestDto;
import place.run.mep.century20.dto.RegisterRequestDto;
import place.run.mep.century20.dto.TokenResponseDto;
import place.run.mep.century20.dto.TokenRefreshRequestDto;
import place.run.mep.century20.dto.LogoutRequestDto;
import place.run.mep.century20.service.AuthService;
import place.run.mep.century20.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import place.run.mep.century20.config.JwtTokenProvider;
import place.run.mep.century20.entity.User;
import place.run.mep.century20.service.UserService;
import place.run.mep.century20.service.AuthService;
import place.run.mep.century20.config.TokenValidationResult;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.resolveToken(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Collections.singletonMap("error", "Token is missing"));
            }
            
            TokenValidationResult result = jwtTokenProvider.validateToken(token);
            if (!result.isValid()) {
                return ResponseEntity.status(result.status())
                    .body(java.util.Collections.singletonMap("error", result.message()));
            }
            return ResponseEntity.ok(java.util.Collections.singletonMap("valid", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Collections.singletonMap("error", "Internal server error"));
        }
    }

    private final UserService userService;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        try {
            userService.registerUser(registerRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\"message\": \"회원 가입이 완료되었습니다.\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            TokenResponseDto tokenResponseDto = authService.login(loginRequestDto);
            return ResponseEntity.ok(tokenResponseDto);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Invalid credentials\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequestDto logoutRequestDto) {
        authService.logout(logoutRequestDto.getRefreshToken());
        return ResponseEntity.ok("{\"message\": \"로그아웃 되었습니다.\"}");
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenResponseDto> refreshToken(@Valid @RequestBody TokenRefreshRequestDto tokenRefreshRequestDto) {
        TokenResponseDto tokenResponseDto = authService.refreshToken(tokenRefreshRequestDto);
        return ResponseEntity.ok(tokenResponseDto);
    }

    @GetMapping("/check-id/{userId}")
    public ResponseEntity<?> checkUserId(@PathVariable String userId) {
        boolean exists = userService.existsByUserId(userId);
        return ResponseEntity.ok(java.util.Collections.singletonMap("exists", exists));
    }
}
