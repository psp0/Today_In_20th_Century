package place.run.mep.century20.service;

import place.run.mep.century20.dto.LoginRequestDto;
import place.run.mep.century20.dto.TokenResponseDto;
import place.run.mep.century20.dto.TokenRefreshRequestDto;

public interface AuthService {
    TokenResponseDto login(LoginRequestDto loginRequestDto);
    TokenResponseDto refreshToken(TokenRefreshRequestDto tokenRefreshRequestDto);
    void logout(String refreshToken);
}
