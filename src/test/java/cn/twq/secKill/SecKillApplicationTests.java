package cn.twq.secKill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SecKillApplicationTests {

  @Autowired private RedisTemplate<String, Object> redisTemplate;
  @Autowired private RedisScript<Long> redisScript;

  @Test
  void test1() {
    ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
    // 如果不存在 key ，才可以设置 value 并返回 true
    // 模拟“锁”的机制
    Boolean isLock = opsForValue.setIfAbsent("k1", "v1");
    if (Boolean.TRUE.equals(isLock)) {
      opsForValue.set("name", "twq");
      String name = (String) opsForValue.get("name");
      System.out.println("name = " + name);
      // 释放锁
      int i = 1 / 0;
      redisTemplate.delete("k1");
    } else {
      System.out.println("已被占用");
    }
  }

  @Test
  void test2() {
    ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
    // 如果不存在 key ，才可以设置 value 并返回 true
    // 模拟“锁”的机制
    String value = UUID.randomUUID().toString();
    Boolean isLock =
        opsForValue.setIfAbsent("k1", value, 500L, TimeUnit.SECONDS);
    if (Boolean.TRUE.equals(isLock)) {
      opsForValue.set("name", "twq");
      System.out.println("name = " + opsForValue.get("name"));
      System.out.println("value = " + opsForValue.get("k1"));
      // 释放锁
      Long result = redisTemplate.execute(redisScript, Collections.singletonList("k1"), value);
      System.out.println(result);
    } else {
      System.out.println("已被占用");
    }
  }
}
