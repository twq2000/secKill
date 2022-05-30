package cn.twq.secKill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 用户id，使用手机号码
   */
  private Long id;

  private String username;

  /**
   * MD5(MD5(明文+固定salt)+随机salt)
   */
  private String password;

  private String salt;

  /**
   * 用户头像
   */
  private String img;

  /**
   * 用户注册时间
   */
  private Date registerDate;

  /**
   * 最后一次登录时间
   */
  private Date lastLoginDate;

  /**
   * 用户登录次数
   */
  private Integer loginCount;


}
