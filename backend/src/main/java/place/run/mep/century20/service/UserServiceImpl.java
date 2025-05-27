package place.run.mep.century20.service;

import place.run.mep.century20.dto.RegisterRequestDto;
import place.run.mep.century20.dto.UserInfoDto;
import place.run.mep.century20.dto.UpdateUserDto;
import place.run.mep.century20.dto.PasswordChangeDto;
import place.run.mep.century20.entity.User;
import place.run.mep.century20.entity.UserAuth;
import place.run.mep.century20.entity.UserInfo;
import place.run.mep.century20.repository.UserRepository;
import place.run.mep.century20.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import place.run.mep.century20.exception.UserNotFoundException;
import place.run.mep.century20.exception.DuplicateResourceException;
import place.run.mep.century20.exception.PasswordMismatchException;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registerUser(RegisterRequestDto dto) {
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateResourceException("이미 사용 중인 아이디입니다.");
        }
        
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        UserInfo userInfo = new UserInfo();
        userInfo.setNickname(dto.getNickname());
        userInfo.setPhone(dto.getPhone());
        userInfo.setEmail(dto.getEmail());
        userInfo.setBirthDate(dto.getBirthDate());
        userInfo.setGender(dto.getGender());
        userInfo.setUser(user);
        user.setUserInfo(userInfo);
        
        userRepository.save(user);

        UserAuth userAuth = new UserAuth(user);
        userAuth.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        userAuthRepository.save(userAuth);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoDto getUserInfo(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        UserInfo userInfo = user.getUserInfo();
        if (userInfo == null) {
            throw new UserNotFoundException("사용자 정보를 찾을 수 없습니다.");
        }
        
        return UserInfoDto.builder()
                .userId(user.getUserId())
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .birthDate(userInfo.getBirthDate())
                .gender(userInfo.getGender())
                .build();
    }

    @Override
    @Transactional
    public UserInfoDto updateUser(String userId, UpdateUserDto dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        UserInfo userInfo = user.getUserInfo();
        if (userInfo == null) {
            throw new UserNotFoundException("사용자 정보를 찾을 수 없습니다.");
        }
        
        if (dto.getNickname() != null) userInfo.setNickname(dto.getNickname());
        if (dto.getPhone() != null) userInfo.setPhone(dto.getPhone());
        if (dto.getName() != null) userInfo.setName(dto.getName());
        if (dto.getEmail() != null) userInfo.setEmail(dto.getEmail());
        if (dto.getBirthDate() != null) {
            userInfo.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        }
        if (dto.getGender() != null) userInfo.setGender(dto.getGender());
        
        userRepository.save(user);
        
        return UserInfoDto.builder()
                .userId(user.getUserId())
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .birthDate(userInfo.getBirthDate())
                .gender(userInfo.getGender())
                .build();
    }

    @Override
    @Transactional
    public void updatePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        UserAuth userAuth = userAuthRepository.findByUser(user);
        if (!passwordEncoder.matches(currentPassword, userAuth.getPasswordHash())) {
            throw new PasswordMismatchException("현재 비밀번호가 일치하지 않습니다.");
        }
        userAuth.setPasswordHash(passwordEncoder.encode(newPassword));
        userAuthRepository.save(userAuth);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        userAuthRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }
}
