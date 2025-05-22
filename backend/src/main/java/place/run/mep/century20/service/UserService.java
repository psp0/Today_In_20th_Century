package place.run.mep.century20;

import place.run.mep.century20.dto.RegisterRequestDto;
import place.run.mep.century20.dto.UserInfoDto;
import place.run.mep.century20.dto.UpdateUserDto;
import place.run.mep.century20.dto.PasswordChangeDto;

public interface UserService {
    void registerUser(RegisterRequestDto registerRequestDto);
    UserInfoDto getUserInfo(String userId);
    UserInfoDto updateUser(String userId, UpdateUserDto updateUserDto);
    void deleteUser(String userId);
    void updatePassword(String userId, PasswordChangeDto passwordChangeDto);
}
