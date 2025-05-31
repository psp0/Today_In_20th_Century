package place.run.mep.century20.service;

import place.run.mep.century20.entity.User;
import place.run.mep.century20.entity.UserAuth;
import place.run.mep.century20.repository.UserAuthRepository;
import place.run.mep.century20.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userId));

        UserAuth userAuth = userAuthRepository.findByUser(user)
                .orElseThrow(() -> new UsernameNotFoundException("User auth record not found for userId: " + userId));

        // 모든 사용자를 ROLE_USER로 반환
        Collection<? extends GrantedAuthority> authorities = java.util.Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                userAuth.getPasswordHash(),
                authorities
        );
    }
}
