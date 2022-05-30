package cn.twq.secKill.controller;

import cn.twq.secKill.service.IUserService;
import cn.twq.secKill.vo.LoginVO;
import cn.twq.secKill.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

  private final IUserService userService;

  public LoginController(IUserService userService) {
    this.userService = userService;
  }

  /**
   * 访问登录页面
   *
   * @return
   */
  @RequestMapping("/toLogin")
  public String toLogin() {
    return "login";
  }

  /**
   * 登录功能
   *
   * @param loginVO 使用@Valid spring-validator校验技术，对前台传回来的数据进行合法性判断
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/doLogin")
  @ResponseBody
  public Result doLogin(
      @Valid LoginVO loginVO, HttpServletRequest request, HttpServletResponse response) {
    return userService.doLogin(loginVO, request, response);
  }
}
