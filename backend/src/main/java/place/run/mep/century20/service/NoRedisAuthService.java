package place.run.mep.century20.service;

import place.run.mep.century20.config.JwtTokenProvider;
import place.run.mep.century20.dto.LoginRequestDto;
import place.run.mep.century20.dto.TokenResponseDto;
import place.run.mep.century20.dto.TokenRefreshRequestDto;
import place.run.mep.century20.entity.User;
import place.run.mep.century20.entity.UserRefreshToken;
import java.util.List;
import place.run.mep.century20.repository.UserRefreshTokenRepository;
import place.run.mep.century20.repository.UserRepository;
import place.run.mep.century20.service.UserDetailsServiceImpl;
import place.run.mep.century20.config.TokenValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Service
@Profile("noredis")
@RequiredArgsConstructor
public class NoRedisAuthService implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserRepository userRepository;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    @Transactional
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUserId(), loginRequestDto.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userDetails.getUsername()));

        // Clean up existing active refresh tokens
        List<UserRefreshToken> existingTokens = userRefreshTokenRepository.findAllByUser_UserIdAndRevokedFalse(user.getUserId());
        if (!existingTokens.isEmpty()) {
            userRefreshTokenRepository.deleteAllInBatch(existingTokens);
        }

        String refreshToken = jwtTokenProvider.createRefreshToken(loginRequestDto.getUserId());
        UserRefreshToken refreshTokenRecord = new UserRefreshToken();
        refreshTokenRecord.setUser(user);
        refreshTokenRecord.setRefreshToken(refreshToken);
        refreshTokenRecord.setIssuedAt(LocalDateTime.now());
        refreshTokenRecord.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenValidityInSeconds()));
        refreshTokenRecord.setRevoked(false);
        userRefreshTokenRepository.save(refreshTokenRecord);

        String accessToken = jwtTokenProvider.createAccessToken(userDetails.getUsername(), java.util.Collections.emptyMap());
        return new TokenResponseDto(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public TokenResponseDto refreshToken(TokenRefreshRequestDto tokenRefreshRequestDto) {
        // Refresh Token 검증
        UserRefreshToken refreshTokenRecord = userRefreshTokenRepository.findByRefreshToken(tokenRefreshRequestDto.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Refresh Token이 만료되었거나 취소된 경우 예외 발생
        if (refreshTokenRecord.isRevoked() || refreshTokenRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token is invalid");
        }

        // 새로운 Access Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(refreshTokenRecord.getUser().getUserId(), java.util.Collections.emptyMap());
        return new TokenResponseDto(accessToken, tokenRefreshRequestDto.getRefreshToken());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        UserRefreshToken refreshTokenRecord = userRefreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        refreshTokenRecord.setRevoked(true);
        refreshTokenRecord.setExpiresAt(LocalDateTime.now());
        userRefreshTokenRepository.save(refreshTokenRecord);
    }

    public TokenValidationResult validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }
}
