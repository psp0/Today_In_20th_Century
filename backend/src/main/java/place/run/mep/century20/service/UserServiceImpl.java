package place.run.mep.century20;

import place.run.mep.century20.dto.RegisterRequestDto;
import place.run.mep.century20.dto.UserInfoDto;
import place.run.mep.century20.dto.UpdateUserDto;
import place.run.mep.century20.entity.User;
import place.run.mep.century20.entity.UserAuth;
import place.run.mep.century20.entity.UserProfile;
import place.run.mep.century20.repository.UserAuthRepository;
import place.run.mep.century20.repository.UserProfileRepository;
import place.run.mep.century20.repository.UserRepository;
import place.run.mep.century20.exception.UserNotFoundException;
import place.run.mep.century20.exception.DuplicateResourceException;
import place.run.mep.century20.exception.PasswordMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void registerUser(RegisterRequestDto registerRequestDto) {
        if (!registerRequestDto.getPassword().equals(registerRequestDto.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match.");
        }
        if (userRepository.existsByUserId(registerRequestDto.getUserId())) {
            throw new DuplicateResourceException("User ID already exists.");
        }
        if (userProfileRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists.");
        }
        if (userProfileRepository.existsByNickname(registerRequestDto.getNickname())) {
            throw new DuplicateResourceException("Nickname already exists.");
        }

        User user = new User();
        user.setUserId(registerRequestDto.getUserId());
        userRepository.save(user);

        UserAuth userAuth = new UserAuth();
        userAuth.setUser(user);
        userAuth.setPasswordHash(passwordEncoder.encode(registerRequestDto.getPassword()));
        userAuthRepository.save(userAuth);

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);
        userProfile.setEmail(registerRequestDto.getEmail());
        userProfile.setPhone(registerRequestDto.getPhone());
        userProfile.setName(registerRequestDto.getName());
        userProfile.setNickname(registerRequestDto.getNickname());
        userProfile.setBirthDate(registerRequestDto.getBirthDate());
        userProfile.setGender(registerRequestDto.getGender());
        userProfileRepository.save(userProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoDto getUserInfo(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        UserProfile userProfile = userProfileRepository.findById(user.getUserNo())
                .orElseThrow(() -> new UserNotFoundException("User profile not found"));

        return UserInfoDto.builder()
                .userId(user.getUserId())
                .email(userProfile.getEmail())
                .phone(userProfile.getPhone())
                .name(userProfile.getName())
                .nickname(userProfile.getNickname())
                .birthDate(userProfile.getBirthDate())
                .gender(userProfile.getGender())
                .build();
    }

    @Override
    @Transactional
    public UserInfoDto updateUser(String userId, UpdateUserDto updateUserDto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        UserProfile userProfile = userProfileRepository.findById(user.getUserNo())
                .orElseThrow(() -> new UserNotFoundException("User profile not found"));

        // 중복 체크 (자기 자신 제외)
        if (userProfileRepository.existsByEmailAndUserNoNot(updateUserDto.getEmail(), user.getUserNo())) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (userProfileRepository.existsByNicknameAndUserNoNot(updateUserDto.getNickname(), user.getUserNo())) {
            throw new DuplicateResourceException("Nickname already exists");
        }

        // 프로필 업데이트
        userProfile.setEmail(updateUserDto.getEmail());
        userProfile.setNickname(updateUserDto.getNickname());
        userProfile.setPhone(updateUserDto.getPhone());
        userProfile.setName(updateUserDto.getName());
        userProfile.setBirthDate(updateUserDto.getBirthDate());
        userProfile.setGender(updateUserDto.getGender());
        userProfileRepository.save(userProfile);

        return UserInfoDto.builder()
                .userId(user.getUserId())
                .email(userProfile.getEmail())
                .phone(userProfile.getPhone())
                .name(userProfile.getName())
                .nickname(userProfile.getNickname())
                .birthDate(userProfile.getBirthDate())
                .gender(userProfile.getGender())
                .build();
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 연관된 모든 데이터 삭제
        userAuthRepository.deleteByUser(user);
        userProfileRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void updatePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        UserAuth userAuth = userAuthRepository.findByUser(user)
                .orElseThrow(() -> new UserNotFoundException("User auth not found"));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, userAuth.getPasswordHash())) {
            throw new PasswordMismatchException("Current password is incorrect");
        }

        // 비밀번호 변경
        userAuth.setPasswordHash(passwordEncoder.encode(newPassword));
        userAuthRepository.save(userAuth);
    }
}
