package cn.twq.secKill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/** redis配置类 */

@Configuration
public class RedisConfig {

  /**
   * 对于存储的数据，进行序列化操作
   *
   * @param factory
   * @return
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    // 普通数据的 key&value 序列化
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setValueSerializer(jsonRedisSerializer);
    // 对于哈希数据，其自身也带有 key&value ，需要对其进行序列化
    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    redisTemplate.setHashValueSerializer(jsonRedisSerializer);
    // 传入连接工厂对象
    redisTemplate.setConnectionFactory(factory);
    return redisTemplate;
  }

  @Bean
  public DefaultRedisScript<Long> redisScript() {
    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
    redisScript.setLocation(new ClassPathResource("stock.lua"));
    redisScript.setResultType(Long.class);
    return redisScript;
  }
}
