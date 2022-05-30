package cn.twq.secKill.vo;

import cn.twq.secKill.validator.Mobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class LoginVO {
  @NotNull(message = "用户名或密码错误！")
  @Mobile
  private String mobile;
  @NotNull(message = "用户名或密码错误！")
  @Length(min = 32)
  private String password;
}
