package place.run.mep.century20.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("noredis")
public class NoRedisUserCacheService implements UserCacheServiceInterface {

    @Override
    public void cacheUserInfo(String userId, String userInfo) {
        // Redis 미사용 시 캐시하지 않음
    }

    @Override
    public String getCachedUserInfo(String userId) {
        return null;
    }

    @Override
    public void invalidateUserCache(String userId) {
        // Redis 미사용 시 무시
    }

    @Override
    public boolean isUserCached(String userId) {
        return false;
    }
}
