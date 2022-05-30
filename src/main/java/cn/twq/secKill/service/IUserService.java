package cn.twq.secKill.service;

import cn.twq.secKill.entity.User;
import cn.twq.secKill.vo.LoginVO;
import cn.twq.secKill.vo.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IUserService extends IService<User> {

  /**
   * 登录功能
   *
   * @param loginVO
   * @param request
   * @param response
   * @return
   */
  Result doLogin(LoginVO loginVO, HttpServletRequest request, HttpServletResponse response);

  /**
   * 根据 cookie 获取用户的具体信息
   *
   * @param loginTicket
   * @param request
   * @param response
   * @return
   */
  User getUserByCookie(
      String loginTicket, HttpServletRequest request, HttpServletResponse response);

  /**
   * 修改密码
   *
   * @param loginTicket 修改密码的前提是用户已登录。如果用户已登录，那么就存在对应的 loginTicket 。然后根据这个来获取用户的具体信息
   * @param password
   * @param request
   * @param response
   * @return
   */
  Result updatePassword(
      String loginTicket,
      String password,
      HttpServletRequest request,
      HttpServletResponse response);
}
