package org.in.com.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.in.com.cache.redis.RedisCacheTypeEM;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableCaching
public class RedisConfig {

	@Value("${redis.hostName}")
	private String redisHostName;

	@Value("${redis.port}")
	private int port;

	@Value("${redis.max.connection}")
	private int maxRedisConnection;

	private Map<String, Long> cacheDurationMap = new HashMap<>();

	@PostConstruct
	public void init() {
		RedisCacheTypeEM[] values = RedisCacheTypeEM.values();
		for (RedisCacheTypeEM cacheType : values) {
			cacheDurationMap.put(cacheType.getCode(), (long) (cacheType.getTimeToLive() * 60));
		}
	}

	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setHostName(redisHostName);
		System.out.println("*************** Redis host name ********" + factory.getHostName());
		factory.setPort(port);
		factory.setUsePool(true);
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		if (maxRedisConnection <= 0) {
			maxRedisConnection = 100;
		}
		poolConfig.setMaxTotal(maxRedisConnection);
		factory.setPoolConfig(poolConfig);
		return factory;
	}

	@Bean
	RedisTemplate<Object, Object> redisTemplate() {
		RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setEnableDefaultSerializer(true);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	@Bean
	CacheManager cacheManager() {
		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate());
		if (!cacheDurationMap.isEmpty()) {
			cacheManager.setExpires(cacheDurationMap);
		}
		return cacheManager;
	}

}