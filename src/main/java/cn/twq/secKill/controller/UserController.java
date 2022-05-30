package cn.twq.secKill.controller;

import cn.twq.secKill.entity.User;
import cn.twq.secKill.vo.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

  /**
   * 获取用户信息（测试使用）
   *
   * @param user
   * @return
   */
  @RequestMapping("/info")
  @ResponseBody
  public Result getInfo(User user) {
    return Result.success(user);
  }

}
