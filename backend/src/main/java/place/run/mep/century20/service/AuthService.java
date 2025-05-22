package place.run.mep.century20;

import place.run.mep.century20.dto.LoginRequestDto;
import place.run.mep.century20.dto.TokenResponseDto;
import place.run.mep.century20.dto.TokenRefreshRequestDto;

public interface AuthService {
    TokenResponseDto login(LoginRequestDto loginRequestDto); // Changed return type
    TokenResponseDto refreshToken(TokenRefreshRequestDto tokenRefreshRequestDto); // Changed return type
    // void register(RegisterRequestDto registerRequestDto); // This is currently in UserService
}
