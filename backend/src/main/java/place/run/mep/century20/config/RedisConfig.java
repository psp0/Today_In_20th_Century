package place.run.mep.century20.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@Profile("redis") // 이 설정은 "redis" 프로필이 활성화될 때만 적용됩니다.
public class RedisConfig {

    private final RedisProperties redisProperties;

    // 생성자를 통해 RedisProperties 객체를 주입받습니다.
    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * Redis 연결 팩토리를 생성합니다.
     * spring.redis.mode 값에 따라 Standalone 또는 Cluster 모드로 연결을 설정합니다.
     * @return RedisConnectionFactory 인스턴스
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Jedis Pool 설정
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(100); // 최대 커넥션 수
        poolConfig.setMaxIdle(50);   // 최대 유휴 커넥션 수
        poolConfig.setMinIdle(10);   // 최소 유휴 커넥션 수
        poolConfig.setMaxWaitMillis(5000); // 커넥션 대기 시간

        // "cluster" 모드일 경우
        if ("cluster".equalsIgnoreCase(redisProperties.getMode())) {
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(redisProperties.getCluster().getNodes());
            String password = redisProperties.getPassword();
            // 비밀번호가 설정된 경우
            if (password != null && !password.isEmpty()) {
                clusterConfig.setPassword(password);
            }
            return new JedisConnectionFactory(clusterConfig, poolConfig);
        } 
        // "standalone" 모드 또는 기본값일 경우
        else {
            RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
            standaloneConfig.setHostName(redisProperties.getHost());
            standaloneConfig.setPort(redisProperties.getPort());
            String password = redisProperties.getPassword();
            // 비밀번호가 설정된 경우
            if (password != null && !password.isEmpty()) {
                standaloneConfig.setPassword(password);
            }
            JedisConnectionFactory factory = new JedisConnectionFactory(standaloneConfig);
            factory.setPoolConfig(poolConfig); // Pool 설정 적용
            return factory;
        }
    }

    /**
     * Redis 작업을 위한 RedisTemplate을 설정합니다.
     * Key는 String, Value는 JSON 형태로 직렬화합니다.
     * @return RedisTemplate<String, Object> 인스턴스
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // Key 직렬화 방식: String
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // Value 직렬화 방식: JSON
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }
}
