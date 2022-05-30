package cn.twq.secKill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.twq.secKill.mapper")
public class SecKillApplication {

  public static void main(String[] args) {
    SpringApplication.run(SecKillApplication.class, args);
  }
}
