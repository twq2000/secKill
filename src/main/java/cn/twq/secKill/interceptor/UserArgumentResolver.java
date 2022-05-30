package cn.twq.secKill.interceptor;

import cn.twq.secKill.entity.User;
import cn.twq.secKill.service.IUserService;
import cn.twq.secKill.util.CookieUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 用户参数解析器。对于所有 User 类型的 controller层 形参，进行公共的解析逻辑——从redis中获取已登录的用户数据，并注入 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

  private final IUserService userService;

  public UserArgumentResolver(IUserService userService) {
    this.userService = userService;
  }

  /**
   * 这是一个过滤方法，用以判断传入的参数是否适用于该解析器。
   * 只有返回结果为 true ，才会执行下面的 resolveArgument() 解析逻辑
   *
   * @param parameter
   * @return
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType() == User.class;
  }

  /**
   * 具体的解析逻辑
   *
   * @param parameter
   * @param mavContainer
   * @param webRequest
   * @param binderFactory
   * @return 返回赋值给 controller层 方法上的形参对象
   */
  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    // 将登录时事先存储在 cookie 中的数据取出来
    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
    HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
    String loginTicket = CookieUtils.getCookieValue(request, "loginTicket");
    // 根据该 cookie ，从 redis 中查询得到对应的用户数据
    return userService.getUserByCookie(loginTicket, request, response);
  }
}
