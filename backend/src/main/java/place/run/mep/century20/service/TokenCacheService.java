package place.run.mep.century20.service;

public interface TokenCacheService {
    void cacheToken(String token, String userId, long expirationTime);
    void invalidateToken(String token);
    boolean isTokenValid(String token);
    String getUsernameFromToken(String token);
}
