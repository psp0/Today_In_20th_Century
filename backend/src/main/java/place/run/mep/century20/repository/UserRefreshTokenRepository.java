package place.run.mep.century20.repository;

import place.run.mep.century20.entity.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {
    Optional<UserRefreshToken> findByRefreshToken(String refreshToken);
    Optional<UserRefreshToken> findByUser_UserIdAndRevokedFalse(String userId);
    List<UserRefreshToken> findAllByUser_UserIdAndRevokedFalse(String userId);
    void deleteByUser_UserNo(Long userNo);
    void deleteAllInBatch(Iterable<UserRefreshToken> entities);
}
