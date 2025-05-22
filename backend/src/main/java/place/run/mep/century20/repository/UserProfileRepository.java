package place.run.mep.century20;

import place.run.mep.century20.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByEmailAndUserNoNot(String email, Long userNo);
    boolean existsByNicknameAndUserNoNot(String nickname, Long userNo);
}
