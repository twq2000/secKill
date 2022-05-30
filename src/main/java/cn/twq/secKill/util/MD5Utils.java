package cn.twq.secKill.util;

import org.apache.commons.codec.digest.DigestUtils;

public final class MD5Utils {

  private static final String SALT = "1a2b3c4d";

  /**
   * 第一次 MD5 加密处理
   * MD5(明文密码+固定SALT)
   *
   * @param password 前端传回来的明文密码
   * @return
   */
  public static String encodePassword2Backend(String password) {
    StringBuilder sb = new StringBuilder();
    password =
        sb
            .append(SALT.charAt(0))
            .append(SALT.charAt(2))
            .append(password)
            .append(SALT.charAt(5))
            .append(SALT.charAt(4))
            .toString();
    return DigestUtils.md5Hex(password);
  }

  /**
   * 第二次 MD5 加密处理
   * MD5(暗文密码+随机salt)
   *
   * @param password 已在后端进行过一次加密处理的暗文密码
   * @param salt 随机salt
   * @return
   */
  public static String encodePassword2DB(String password, String salt) {
    StringBuilder sb = new StringBuilder();
    password =
        sb
            .append(salt.charAt(0))
            .append(salt.charAt(2))
            .append(password)
            .append(salt.charAt(5))
            .append(salt.charAt(4))
            .toString();
    return DigestUtils.md5Hex(password);
  }

  /**
   * 最终调用的 MD5 加密方法, 包含了两次加密
   * MD5(MD5(明文密码+固定SALT)+随机salt)
   *
   * @param password 前端传回来的明文密码
   * @param salt 随机salt
   * @return
   */
  public static String encodePassword(String password, String salt) {
    return encodePassword2DB(encodePassword2Backend(password), salt);
  }

  public static void main(String[] args) {
    System.out.println(encodePassword2Backend("123456"));
    System.out.println(encodePassword("123456", "1a2b3c4d"));
  }

}
