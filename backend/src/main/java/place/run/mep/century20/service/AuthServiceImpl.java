package place.run.mep.century20;

import place.run.mep.century20.config.JwtTokenProvider;
import place.run.mep.century20.dto.LoginRequestDto;
import place.run.mep.century20.dto.TokenResponseDto;
import place.run.mep.century20.dto.TokenRefreshRequestDto;
import place.run.mep.century20.entity.User;
import place.run.mep.century20.entity.UserRefreshToken;
import place.run.mep.century20.repository.UserRefreshTokenRepository;
import place.run.mep.century20.repository.UserRepository;
import place.run.mep.century20.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

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
        
        String accessToken = jwtTokenProvider.createAccessToken(authentication); 
        String refreshTokenString = jwtTokenProvider.createRefreshToken(authentication); 

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userDetails.getUsername()));

        UserRefreshToken userRefreshToken = userRefreshTokenRepository.findByUser_UserId(user.getUserId())
                .orElse(new UserRefreshToken()); 
        
        userRefreshToken.setUser(user);
        userRefreshToken.setRefreshToken(refreshTokenString);
        userRefreshToken.setIssuedAt(LocalDateTime.now()); 
        userRefreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenValidityInSeconds())); 
        userRefreshToken.setRevoked(false);
        userRefreshTokenRepository.save(userRefreshToken);

        return new TokenResponseDto(accessToken, refreshTokenString); 
    }

    @Override
    @Transactional
    public TokenResponseDto refreshToken(TokenRefreshRequestDto tokenRefreshRequestDto) {
        String requestRefreshToken = tokenRefreshRequestDto.getRefreshToken();
        
        if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        String userId = jwtTokenProvider.getUsernameFromToken(requestRefreshToken);
        UserRefreshToken storedRefreshToken = userRefreshTokenRepository.findByUser_UserId(userId) 
                .orElseThrow(() -> new RuntimeException("Refresh token not found in DB"));

        if (storedRefreshToken.isRevoked() || storedRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token has been revoked or expired");
        }

        if (!storedRefreshToken.getRefreshToken().equals(requestRefreshToken)) {
            throw new RuntimeException("Refresh token mismatch");
        }

        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(userId);
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String newAccessToken = jwtTokenProvider.createAccessToken(newAuthentication); 

        return new TokenResponseDto(newAccessToken, requestRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        UserRefreshToken refreshTokenRecord = userRefreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        
        refreshTokenRecord.setRevoked(true);
        refreshTokenRecord.setExpiresAt(LocalDateTime.now());
        userRefreshTokenRepository.save(refreshTokenRecord);
    }
}
