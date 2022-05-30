package cn.twq.secKill.config;

import cn.twq.secKill.entity.User;

public class UserContext {

  private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

  public static void setUser(User user) {
    USER_HOLDER.set(user);
  }

  public static User getUser() {
    return USER_HOLDER.get();
  }
}
