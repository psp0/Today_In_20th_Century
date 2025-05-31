package place.run.mep.century20.service;

import place.run.mep.century20.config.JwtTokenProvider;
import place.run.mep.century20.dto.LoginRequestDto;
import place.run.mep.century20.dto.TokenResponseDto;
import place.run.mep.century20.dto.TokenRefreshRequestDto;
import place.run.mep.century20.entity.User;
import place.run.mep.century20.entity.UserRefreshToken;
import place.run.mep.century20.repository.UserRefreshTokenRepository;
import place.run.mep.century20.repository.UserRepository;
import place.run.mep.century20.service.UserDetailsServiceImpl;
import place.run.mep.century20.config.TokenValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("redis")
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserRepository userRepository;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final TokenCacheService tokenCacheService;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserRefreshTokenRepository userRefreshTokenRepository, UserRepository userRepository, UserDetailsServiceImpl userDetailsServiceImpl, TokenCacheService tokenCacheService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRefreshTokenRepository = userRefreshTokenRepository;
        this.userRepository = userRepository;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        UserRefreshToken refreshTokenEntity = userRefreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        refreshTokenEntity.setRevoked(true);
        userRefreshTokenRepository.save(refreshTokenEntity);
    }

    @Override
    @Transactional
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUserId(), loginRequestDto.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userDetails.getUsername()));

        // Delete all existing active refresh tokens
        List<UserRefreshToken> existingTokens = userRefreshTokenRepository.findAllByUser_UserIdAndRevokedFalse(user.getUserId());
        if (!existingTokens.isEmpty()) {
            userRefreshTokenRepository.deleteAllInBatch(existingTokens);
        }

        // Create new tokens
        String accessToken = jwtTokenProvider.createAccessToken(userDetails.getUsername(), java.util.Collections.emptyMap());
        String refreshTokenString = jwtTokenProvider.createRefreshToken(userDetails.getUsername());

        // Save new refresh token
        UserRefreshToken userRefreshToken = new UserRefreshToken();
        userRefreshToken.setUser(user);
        userRefreshToken.setRefreshToken(refreshTokenString);
        userRefreshToken.setIssuedAt(LocalDateTime.now());
        userRefreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenValidityInSeconds()));
        userRefreshToken.setRevoked(false);
        userRefreshTokenRepository.save(userRefreshToken);

        // Cache access token in Redis
        long accessTokenValidityMs = jwtTokenProvider.getAccessTokenExpirationMs();
        tokenCacheService.cacheToken(accessToken, userDetails.getUsername(), accessTokenValidityMs);

        return new TokenResponseDto(accessToken, refreshTokenString);
    }

    @Override
    @Transactional
    public TokenResponseDto refreshToken(TokenRefreshRequestDto tokenRefreshRequestDto) {
        String requestRefreshToken = tokenRefreshRequestDto.getRefreshToken();

        // Validate JWT token first
        TokenValidationResult result = jwtTokenProvider.validateToken(requestRefreshToken);
        if (!result.isValid()) {
            throw new RuntimeException("Invalid Refresh Token (JWT validation failed)");
        }

        String userId = jwtTokenProvider.getUsernameFromToken(requestRefreshToken);
        
        // Find existing token by exact token string
        UserRefreshToken existingRefreshToken = userRefreshTokenRepository.findByRefreshToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found in DB"));

        // Validate token ownership and status
        if (!existingRefreshToken.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Refresh token user mismatch");
        }

        if (existingRefreshToken.isRevoked() || existingRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token has been revoked or expired");
        }

        // Reissue access token
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, java.util.Collections.emptyMap());
        return new TokenResponseDto(newAccessToken, requestRefreshToken);
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Get username from token
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        
        // Find and revoke existing token
        UserRefreshToken refreshTokenRecord = userRefreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Refresh Token을 DB에서 무효화
        refreshTokenRecord.setRevoked(true);
        refreshTokenRecord.setExpiresAt(LocalDateTime.now());
        userRefreshTokenRepository.save(refreshTokenRecord);

        // Redis에서 Access Token 삭제
        if (tokenCacheService != null && accessToken != null) {
            tokenCacheService.invalidateToken(accessToken);
        }
    }
}
