package place.run.mep.century20;

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
import java.util.Collections;

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

        UserAuth userAuth = userAuthRepository.findById(user.getUserNo())
                .orElseThrow(() -> new UsernameNotFoundException("User auth record not found for userId: " + userId));

        Collection<? extends GrantedAuthority> authorities = userAuth.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                userAuth.getPasswordHash(), // Changed from getPassword()
                authorities
        );
    }
}
