package place.run.mep.century20.service;

public interface UserCacheServiceInterface {
    void cacheUserInfo(String userId, String userInfo);
    String getCachedUserInfo(String userId);
    void invalidateUserCache(String userId);
    boolean isUserCached(String userId);
}
