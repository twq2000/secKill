package cn.twq.secKill.util;

import java.util.UUID;

/** UUID工具类 */
public final class UUIDUtils {

  public static String uuid() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
