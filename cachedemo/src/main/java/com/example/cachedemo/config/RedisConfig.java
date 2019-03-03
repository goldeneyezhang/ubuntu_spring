package com.example.cachedemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.timeout}")
    private int timeout;
    @Value("${spring.redis.password}") private String redisAuth;
    @Value("${spring.redis.database}") private int redisDb;
    @Value("${spring.redis.pool.max-active}") private int maxActive;
    @Value("${spring.redis.pool.max-wait}") private int maxWait;
    @Value("${spring.redis.pool.max-idle}") private int maxIdle;
    @Value("${spring.redis.pool.min-idle}") private int minIdle;


    //自定义缓存key生成策略
//    @Bean
//    public KeyGenerator keyGenerator() {
//        return new KeyGenerator(){
//            @Override
//            public Object generate(Object target, java.lang.reflect.Method method, Object... params) {
//                StringBuffer sb = new StringBuffer();
//                sb.append(target.getClass().getName());
//                sb.append(method.getName());
//                for(Object obj:params){
//                    sb.append(obj.toString());
//                }
//                return sb.toString();
//            }
//        };
//    }

    @Bean
    public RedisConnectionFactory connectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(true);
        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder() .usePooling().poolConfig(poolConfig).and().readTimeout(Duration.ofMillis(timeout)).build();
        //单点redis
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        // 哨兵redis // RedisSentinelConfiguration redisConfig = new RedisSentinelConfiguration();
        // 集群redis // RedisClusterConfiguration redisConfig = new RedisClusterConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPassword(RedisPassword.of(redisAuth));
        redisConfig.setPort(port);
        redisConfig.setDatabase(redisDb);
        return new JedisConnectionFactory(redisConfig,clientConfig); }

    //缓存管理器
    @Bean public CacheManager cacheManager()
    {
        //缓存配置对象
                RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();

                redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofMinutes(30L)); //设置缓存的默认超时时间：30分钟


        return RedisCacheManager
                        .builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory()))
                .cacheDefaults(redisCacheConfiguration).build();
    }
    @Bean
    public RedisTemplate<String, String> redisTemplate(){
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory());


        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);

        ObjectMapper om = new ObjectMapper();

        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(om);


        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
