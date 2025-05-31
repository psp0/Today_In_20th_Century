package place.run.mep.century20.repository;

import place.run.mep.century20.entity.User;
import place.run.mep.century20.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    Optional<UserAuth> findByUser(User user);
    void deleteByUser(User user);
    void deleteByUserNo(Long userNo);
}
